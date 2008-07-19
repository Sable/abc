#!/bin/sh
dos2unix out
dos2unix out.default
diff out out.default
