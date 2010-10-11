/*
 * $Id: rawmode.sql 610 2008-12-22 15:54:18Z unsaved $
 *
 * Tests raw mode
 */

CREATE TABLE t (i INTEGER);
INSERT INTO t values (42);

/** Adding a few blank lines in what is sent to server on purpose. */
\.


SELECT i FROM t


.;

*if (*? != 42)
    \q Raw command failed
*end if
