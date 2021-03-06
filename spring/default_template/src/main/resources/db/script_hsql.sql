CREATE SEQUENCE BOARD_SEQ 
AS INTEGER;


CREATE MEMORY TABLE T_USER
(
	USER_ID 		VARCHAR(100) NOT NULL PRIMARY KEY,
	USER_PWD 		VARCHAR(100) NOT NULL,
	USER_NM 		VARCHAR(100) NOT NULL,
	ROLE 			VARCHAR(100) NOT NULL
);

CREATE MEMORY TABLE T_ROLE_AUTH
(
	ROLE_CD		VARCHAR(100) NOT NULL, 
	URI 		VARCHAR(100) NOT NULL, 
	AUTH 		VARCHAR(100) NOT NULL
);


CREATE MEMORY TABLE T_CODE
(
	CD 			VARCHAR(100) NOT NULL, 
	CD_NM 		VARCHAR(100) NOT NULL,
	GROUP_CD 	VARCHAR(100) NOT NULL
);

CREATE MEMORY TABLE T_BOARD(
	SEQ 		INTEGER GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
	CONTENT 	VARCHAR(100) NOT NULL,
	USER_ID 	VARCHAR(100) NOT NULL,
	PWD 		VARCHAR(100) NOT NULL,
	REGDT 		TIMESTAMP  NOT NULL,
);

