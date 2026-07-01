"""
Bridge between the Android (Kotlin) UI and CPython, executed by Chaquopy.

`run_code` executes user source with stdout/stderr redirected to Kotlin
callbacks, supports input() via a blocking Kotlin callback, and can be
interrupted cooperatively through a `stop` checker.
"""

import sys
import builtins
import traceback


class _Tee:
    """File-like object that forwards writes to a Kotlin callback."""

    def __init__(self, callback):
        self._callback = callback

    def write(self, text):
        if text:
            # Kotlin side accepts a String.
            self._callback.write(str(text))
        return len(text) if text else 0

    def flush(self):
        pass

    def isatty(self):
        return False


def run_code(code, out_cb, err_cb, input_cb, stop):
    """
    Execute `code` as __main__.

    out_cb / err_cb : objects with write(String)
    input_cb        : object with read(String) -> String  (blocks for input)
    stop            : object with stopped() -> bool        (cooperative cancel)
    """
    old_out, old_err, old_in = sys.stdout, sys.stderr, builtins.input
    sys.stdout = _Tee(out_cb)
    sys.stderr = _Tee(err_cb)

    def _input(prompt=""):
        if prompt:
            sys.stdout.write(str(prompt))
        result = input_cb.read(str(prompt) if prompt else "")
        if result is None:
            raise EOFError("input cancelled")
        return str(result)

    builtins.input = _input

    # Cooperative interruption: the trace hook raises KeyboardInterrupt
    # as soon as the user presses Stop. Triggers per line, so even tight
    # loops can be cancelled.
    def _tracer(frame, event, arg):
        if stop.stopped():
            raise KeyboardInterrupt()
        return _tracer

    glb = {"__name__": "__main__", "__builtins__": builtins}

    try:
        sys.settrace(_tracer)
        try:
            compiled = compile(code, "<input>", "exec")
        except SyntaxError:
            sys.settrace(None)
            err_cb.write("".join(traceback.format_exc(limit=0)))
            return
        exec(compiled, glb)
    except KeyboardInterrupt:
        sys.settrace(None)
        err_cb.write("\n[Stopped]\n")
    except SystemExit:
        pass
    except BaseException:
        sys.settrace(None)
        tb = traceback.format_exc()
        # Hide the two internal frames from this runner file.
        err_cb.write(_clean_traceback(tb))
    finally:
        sys.settrace(None)
        sys.stdout, sys.stderr, builtins.input = old_out, old_err, old_in


def _clean_traceback(tb):
    lines = tb.splitlines(keepends=True)
    cleaned = []
    skip = False
    for ln in lines:
        if 'File "<input>"' in ln:
            skip = False
        if "pyrunner.py" in ln and ln.lstrip().startswith("File"):
            skip = True
            continue
        if skip and ln.startswith("    "):
            continue
        skip = False
        cleaned.append(ln)
    return "".join(cleaned)
