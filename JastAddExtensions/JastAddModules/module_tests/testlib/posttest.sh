#!/bin/sh
dos2unix out$1
dos2unix out$1.default
diff out out.default
