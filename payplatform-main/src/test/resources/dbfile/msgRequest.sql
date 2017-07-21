delete from msg_send_request;
insert into msg_send_request (ID, USER_ID, PHONE, MSG, REQUEST_TIME, TYPE, RESERVE_DATA, IS_SEND, APPL_SEQ, PAY_CODE)
values ('bda0069f8df94252a5d30a6edd8ced14', '371522199102140532', '18254561920', '您的支付密码为：650728', to_timestamp('2006-01-01 12:10:10.1','yyyy-mm-dd hh24:mi:ss.ff'), 'payCode', '', '1', '1255266', '650728');

