<!--Hide code 

function validateContractId(thisElement) {
	var ltaid = thisElement.value;
	ltaid = trim(ltaid);
	ltaid = ltaid.toUpperCase();
	thisElement.value = ltaid;
	if (ltaid.length == 0) {
          alert('Please specify the new Contract ID in the space provided.');
          return false;
        }

	if (ltaid.length != 13) {
		alert('Invalid Contract ID : Must be 13 characters.');
		thisElement.focus();
		return false;
	}

  	var valid_chars="ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890" 
	if (!validvalue(ltaid,valid_chars,1)) {
		alert('Invalid Contract ID : Only letters (A-Z) and numbers (0-9) permitted.');
		thisElement.focus();
		return false;
	}
  
	var numString = ltaid.substring(6,8);
	if (isNaN(parseFloat(numString))) {
		alert('Invalid Contract ID : Characters in positions 7 and 8 must be numeric.');
		thisElement.focus();
		return false;
	}

	return true;
}

//Checks for correct data in NSN field. Does not check for blank field; that's done
// at form submission time.
function validateItemId(thisElement) {
	var itemnsn = thisElement.value;
	itemnsn = trim(itemnsn);
	itemnsn = itemnsn.toUpperCase();
	thisElement.value = itemnsn;
	if (itemnsn.length != 0 && itemnsn.length != 13) {
		alert('Invalid item NSN : Must be 13 characters.');
		thisElement.focus();
		return false;
	}
	var valid_chars="ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890" 
	if (validvalue(itemnsn, valid_chars,1)) {
		return true;
	}
	alert('Invalid item NSN : Only letters (A-Z) and numbers (0-9) permitted.');
	thisElement.focus();
	return false;	
}
//Checks for correct data in RDD field, if there's anything there at all. 
function validateRDD(thisElement) {
	var reqDelivDate = thisElement.value;
	reqDelivDate = trim(reqDelivDate );
	reqDelivDate = reqDelivDate.toUpperCase();
	thisElement.value = reqDelivDate;
	if (reqDelivDate.length != 0) {
		if (reqDelivDate.length != 3) {
			alert('Invalid RDD: Must be 3 alphanumeric characters.');
			thisElement.focus();
			return false;
		}
		var valid_chars="1234567890BCDEFGHJKLMNPRSTUVW" 
		if (!validvalue(reqDelivDate.substring(0,1), valid_chars, 1)) {
			alert("Invalid RDD: first character is invalid.");
			thisElement.focus();
			return false;
		}
		var valid_chars="1234567890" 
		if (!validvalue(reqDelivDate.substr(1), valid_chars, 1)) {
			alert("Invalid RDD: second two characters must be numeric.");
			thisElement.focus();
			return false;
		}
	}
	return true;	
}

function validateEntry(thisElement, valid_type, numChars) {
	var entry = thisElement.value;
	entry = trim(entry);
	entry = entry.toUpperCase();
	thisElement.value = entry;
	if (entry.length == 0)
	  return true;
	if (!checkvalues(thisElement, valid_type))
	  return false;
	if (numChars != 0) {
		if (entry.length != numChars) {
		  alert("Invalid number of characters.  Must be " + numChars + " characters.");
		  thisElement.focus();
		  return false;
		}
	}
	return true;
}
	  

function check_uppercase(thisForm) {
	for (var i=0; i<thisForm.length; i++)
	{
		if ((thisForm.elements[i].type != 'hidden') && (thisForm.elements[i].type != 'submit')) {
			stringname = thisForm.elements[i].value
			thisForm.elements[i].value = stringname.toUpperCase();
	    }
	}
}

function check_4digit_date(thisElement){
/* Check to see if a valid date was entered. */	
	var err=0;
	var valid_type_str="1234567890/";
	var num_str="1234567890";

        var date = thisElement.value;
        date = trim(date);
        thisElement.value = date;

	if (date.length == 0)
		return true;

        if (!validvalue(date,valid_type_str,0))
	{
		alert('Invalid characters in date - (use MM/DD/YYYY format)!');
		thisElement.focus();
		return false;
	}

	slash1 = date.indexOf("/");
	slash2 = date.indexOf("/",slash1+1);
	if (slash1 <= 0 || slash2 <= 0)
	{
		alert('Invalid date format - (use MM/DD/YYYY format)!');
		thisElement.focus();
		return false;
	}
	month = date.substring(0,slash1);
	day = date.substring(slash1+1,slash2);
	year = date.substring(slash2+1,date.length);
        if (!validvalue(month,num_str,0) || !validvalue(day,num_str,0) || !validvalue(year,num_str,0))
	{
		alert('Invalid date format - (use MM/DD/YYYY format)!');
		thisElement.focus();
		return false;
	}
	if ((month.length<1 || month.length>2) ||
	   (day.length<1 || day.length>2) ||
	   (year.length!=4))
	{
		alert('Invalid date format - (use MM/DD/YYYY format)!');
		thisElement.focus();
		return false;
	}

	//basic error checking
	if (month<1 || month>12) err = 1
	if (day<1 || day>31) err = 1
	if (year<0 || year>9999) err = 1
	
	//advanced error checking

	// months with 30 days
	if (month==4 || month==6 || month==9 || month==11){
		if (day==31) err=1
	}

	// february, leap year
	if (month==2 && day>28){

		if (day > 29) err = 1	
		if ((((year/4)!=parseInt(year/4)) || 
		     ((year/100)==parseInt(year/100))) && 
		     ((year/400)!=parseInt(year/400)))
				err = 1
	}

	if (err==1){
		alert('Invalid date (check your calendar)!');
		thisElement.focus();
		return false;
	}
	return true;
}

function trim_value(thisElement) {
	field = thisElement.value;
	field = trim(field);
	thisElement.value = field;
}

/* Function to check field value entered on form */
/* valid_type = 0  alpha-numberic			*/
/*	          = 1  alphabet only			*/
/*            = 2  numeric only				*/
/*            = 3  alpha-numeric + spaces	*/
/*		      = 4  phone number             */
/*		      = 5  date                     */

function checkvalues(thisElement,valid_type) {

	/* Get value entered by user and strip ld/tr spaces*/
	field=thisElement.value;
	field=trim(field);
	thisElement.value=field;

	if (valid_type == 0) 
		var valid_type_str="abcdefghijklmnopqrstuvwxyz1234567890."
	else if(valid_type == 10)
		var valid_type_str="abcdefghijklmnopqrstuvwxyz1234567890"
	else if(valid_type == 1)
		var valid_type_str="abcdefghijklmnopqrstuvwxyz." 
	else if(valid_type ==2)
		var valid_type_str="1234567890" 
	else if(valid_type == 20)
		var valid_type_str="1234567890bcdefghjklmnprstuvw" 
	else if(valid_type ==3)
		var valid_type_str="abcdefghijklmnopqrstuvwxyz1234567890.,/&$#@*:- " 
	else if(valid_type ==4)
		var valid_type_str="1234567890()-. "
	else if(valid_type ==5){
		return(check_4digit_date(thisElement));
	}
    	else if(valid_type ==6)
		var valid_type_str="abcdefghijklmnopqrstuvwxyz1234567890,&$#@:-" 
    	else if(valid_type ==7)
		var valid_type_str="abcdefghijklmnopqrstuvwxyz " 
    	else if (valid_type == 8)
        	var valid_type_str="abcdefghijklmnopqrstuvwxyz1234567890. " 
	else
        	var valid_type_str="abcdefghijklmnopqrstuvwxyz1234567890_%*?" 

    	/* Check that something was entered if not show error and return false*/
	/* Don't check for null -- check at end when form is submitted. */

    	/* If something entered check field for invalid characters, in this case must */
    	/* be alpha numeric N.B note the last parameter of 0 indicating check is not */
    	/* case sensitive */

    	if (!validvalue(field,valid_type_str,0)) {
    		/* Invalid characters in field so show alert box and return false*/              
      		alert("Invalid characters found in field");
		thisElement.focus();
		return false;
    	}
	return true;
}

/*
================================================================== */
/* Function to strip leading and trailing spaces from value (similar to Java trim method) */
/*================================================================== */
/* Input is string to be trimmed and return value is the result after trimming */

function trim(value) {
	/* Strip leading spaces from input */
    startposn=0;
    while((value.charAt(startposn)==" ")&&(startposn<value.length)) {
		startposn++;
    }
    if(startposn==value.length) {
        value="";
    } 
	else {
    	/* If anything left of string after stripping leading spaces strip trailing */
        /* spaces as well */
        value=value.substring(startposn,value.length);
        endposn=(value.length)-1;
        while(value.charAt(endposn)==" ") {
    		endposn--;
        }
        value=value.substring(0,endposn+1);
	}

    return(value);
}


/* ====================================================================*/
/* Function to check that field value doesn't contain spaces or numbers*/
/* ====================================================================*/
/* Inputs are the string to be checked, the characters to be allowed and flag value indicating */
/* if check on letters is to be case sensitive, where 1 inidcates it is case sensitive */
/* Return code is true is field value is valid, false if not. */

function validvalue(value,validchars,casesensitive) {

	/* If not case sensitive then convert both value and valid */
	/* characters to upper case */
	if (casesensitive!=1) {
		value=value.toUpperCase();
        validchars=validchars.toUpperCase();
    }

    /* Go through each character in value until either end or hit an invalid char */
    charposn=0;
    while ((charposn<value.length)&&(validchars.indexOf(value.charAt(charposn))!=-1)) {
    	charposn++;
    }

	/* Check if stop was due to end of input string or invalid char and set return code */
	/* accordingly */
	if (charposn==value.length) {
        return(true);
	} 
	else {
        return(false);
	}
}

function check_document(entry)  {
	entry.value = entry.value.toUpperCase();
	return(checkvalues(entry, 9));
}

function check_contract(entry)  {
	entry.value = entry.value.toUpperCase();
	return(checkvalues(entry, 9));
}

function check_priority(entry)  {
	return(checkvalues(entry, 2));
}

function check_date(entry)  {
	return(checkvalues(entry, 5));
}

function check_requisition_entry(formentry) {
	if (check_document(formentry.document) && check_priority(formentry.priority_filter) &&
	    check_date(formentry.sdate) && check_date(formentry.edate))
		return true;
	return false;
}	

function check_ordermanage_entry(formentry) {
	if (check_contract(formentry.contract) && check_date(formentry.sdate) && check_date(formentry.edate))
		return true;

	return false;
}	

//Checks a password entry against a parallel, duplicate entry to confirm that the same text was entered
// in both fields.
function confirm_password(thisForm, inputField) {
	var pwValue = thisForm.passwd.value
	//flag for whether field under test is the primary (true) or the confirmation field
	var primary = (inputField.name == "passwd")
	if (primary) {
		//Check for valid values in the password field and
		// clear the confirmation field if primary field is changed
		if (pwValue != thisForm.passwd_confirm.value) {
			if(!checkvalues(inputField,0)) {
				thisForm.passwd_confirm.value = ""
				thisForm.passwd.value = ""
				thisForm.passwd.focus() //this doesn't seem to be working
			} else {
				thisForm.passwd_confirm.value = ""
				thisForm.passwd_confirm.focus() //this doesn't seem to be working
			}
		}
	} else {
		if (pwValue != thisForm.passwd_confirm.value) {
			thisForm.passwd_confirm.value = ""
			thisForm.passwd.value = ""
			thisForm.passwd.focus() //this doesn't seem to be working
			alert("The password confirmation does not match the password.  Please reenter.")
			return false
		}
	}
	return true
}

//-->
