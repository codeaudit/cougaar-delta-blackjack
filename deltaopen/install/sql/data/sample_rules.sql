
insert into temp_qual_rule values(1, 'CheckRecentDefaults', 'Negative', 'N', 'Y', 1, sysdate, sysdate, 'N');
insert into temp_qual_rule values(2, 'CheckIncome', 'Negative', 'N', 'Y', 1, sysdate, sysdate, 'N');

insert into temp_qual_rule_test columns(rule_id, rule_test_id, parent_test_id, logical_operator) values(1, 1, -1, 'AND');
insert into temp_qual_rule_test 
columns(rule_id, rule_test_id, parent_test_id, operand_1_type, operand_1_accessor_id, operand_2_type, operand_2_literal_type, operand_2_literal_value, operator_id ) 
values(1, 2, 1,'Accessor', 103, 'Literal', 'Boolean', 'true', 106);

insert into temp_qual_rule_test columns(rule_id, rule_test_id, parent_test_id, logical_operator) values(2, 3, -1, 'AND');
insert into temp_qual_rule_test 
columns(rule_id, rule_test_id, parent_test_id, operand_1_type, operand_1_accessor_id, operand_2_type, operand_2_accessor_id, operator_id ) 
values(2, 4, 3, 'Accessor', 102, 'Accessor', 107, 101);


