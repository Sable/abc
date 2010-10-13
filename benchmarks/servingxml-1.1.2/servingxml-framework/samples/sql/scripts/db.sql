DROP TABLE EMP_HISTORY;
COMMIT;

CREATE TABLE EMP_HISTORY
(
EMPNO            NUMBER(4) NOT NULL,
ENAME            VARCHAR2(10),
JOB              VARCHAR2(9),
MGR              NUMBER(4),
HIREDATE         DATE,
SAL              NUMBER(7,2),
COMM             NUMBER(7,2),
DEPTNO           NUMBER(2)
);

COMMIT;

DROP TABLE EMP_DISCARD;
COMMIT;

CREATE TABLE EMP_DISCARD
(
  MESSAGE         VARCHAR2(255),
  EMPNO           NUMBER(4) NOT NULL,
  ENAME           VARCHAR2(10),
  JOB             VARCHAR2(9),
  MGR             NUMBER(4),
  HIREDATE        DATE,
  SAL             NUMBER(7,2),
  COMM            NUMBER(7,2),
  DEPTNO          NUMBER(2)
);

COMMIT;
DROP TABLE EMP_LOG;
COMMIT;

CREATE TABLE EMP_LOG
(
  EMPNO           NUMBER(4) NOT NULL,
  ENAME           VARCHAR2(10)
);

COMMIT;

DROP TABLE master;
COMMIT;

CREATE TABLE master
(
  master_id      NUMBER(4) NOT NULL,
  name           VARCHAR2(20)
);

COMMIT;

DROP TABLE detail;
COMMIT;

CREATE TABLE detail
(
  detail_id      NUMBER(4) NOT NULL,
  detail_date    DATE,
  detail_type    VARCHAR2(10)
);

COMMIT;

