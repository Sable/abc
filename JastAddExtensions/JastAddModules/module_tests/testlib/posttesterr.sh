#!/bin/sh
dos2unix out$1 2>/dev/null
dos2unix out$1.default 2>/dev/null
diff out out.default
