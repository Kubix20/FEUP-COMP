package semantic;

/**
	* Stores information about a declaration in the code (need not be global)
	*/
public class Declaration {

	// Declaration's name
	public String name;
	// Declaration type (integer, array, undefined)
	public String type;
	// Access type (integer, array, undefined)
	public String access = "";
	// Is this an array size access?
	public boolean sizeAccess = false;
	// Auxiliar variables to handle different type declarations between if-else branches
	public String ifStatus = "";
	public boolean newIfBranch = false;
	// Is the declaration global?
	public boolean global;
	// Has the variable been initialized?
	public boolean init;
	// The variable's value, in case it is integer
	public int value;
	// The array's size (in case variable is array)
	public int size = 0;
	public int local = -1;

	/**
		* Build a generic declaration
		*/
	public Declaration(){
		init = false;
	}

	/**
		* Build a generic declaration, given its name
		* @param name declaration's name
		*/
	public Declaration(String name){
		this.name = name;
		type = "undefined";
		init = false;
		global = false;
	}

	/**
		* Build a declaration given name, type and global status
		* @param name declaration's name
		* @param type declaration's type
		* @param global declaration's global status (true if global declaration)
		*/
	public Declaration(String name, String type, boolean global){
		this.name = name;
		this.type = type;
		init = false;
		this.global = global;
	}


	/**
		* Build a declaration from an already created declaration
		* @param var other declaration
		*/
	public Declaration(Declaration var){
		name = var.name;
		type = var.type;
		access = var.access;
		ifStatus = var.ifStatus;
		global = var.global;
		init = var.init;
		local = var.value;
	}

	/**
		* Init the declaration
		* @param type declaration type
		*/
	public void init(String type){
		if(type.compareTo("integer") == 0)
			initInt();

		else if(type.compareTo("array") == 0)
			initArray();
	}

	/**
		* Init an integer declaration
		*/
	public void initInt (){
		if(!init)
		{
			init = true;
			type = "integer";
		}
	}

	/**
		* Init an integer declaration with an associated value
		* @param value declaration value
		*/
	public void initInt (int value){
		if(!init)
		{
			init = true;
			this.value = value;
			type = "integer";
		}
	}

	/**
		* Init an array declaration
		*/
	public void initArray(){
		if(!init)
		{
			init = true;
			type = "array";
		}
	}

	/**
		* Init an array declaration with an associated array size
		* @param size array size
		*/
	public void initArray(int size){
		if(!init)
		{
			init = true;
			this.size = size;
			type = "array";
		}
	}

	/**
		* Checks if declaration is initialized
		* @return true if initialized, false otherwise
		*/
	public boolean isInitialized(){
		if(global)
			return true;
		else
			return init;
	}

	/**
		* Checks if declaration is of type integer
		* @return true if integer declaration, false otherwise
		*/
	public boolean isInt(){
		return type.compareTo("integer") == 0;
	}

	/**
		* Checks if declaration is of type array
		* @return true if array declaration, false otherwise
		*/
	public boolean isArray(){
		return type.compareTo("array") == 0;
	}

	/**
		* Checks if declaration is of type undefined
		* @return true if undefined declaration, false otherwise
		*/
	public boolean isUndefined(){
		return type.compareTo("undefined") == 0;
	}

	/**
		* Checks if declaration access is of type integer
		* @return true if integer access declaration, false otherwise
		*/
	public boolean intAccess(){
		return access.compareTo("integer") == 0;
	}

	/**
		* Checks if declaration access is of type array
		* @return true if array access declaration, false otherwise
		*/
	public boolean arrayAccess(){
		return access.compareTo("array") == 0;
	}

	/**
		* Checks if declaration access is of type undefined
		* @return true if undefined access declaration, false otherwise
		*/
	public boolean undefinedAccess(){
		return access.compareTo("undefined") == 0;
	}

	/**
		* Checks if declaration's if status is partial
		* @return true if partial if status, false otherwise
		*/
	public boolean partialIfStatus(){
		return ifStatus.compareTo("partial") == 0;
	}

	/**
		* Checks if declaration's if status is incompatible
		* @return true if incompatible if status, false otherwise
		*/
	public boolean incompatibleIfStatus(){
		return ifStatus.compareTo("incompatible") == 0;
	}

	/**
		* Checks if declaration's if status is complete
		* @return true if complete if status, false otherwise
		*/
	public boolean completeIfStatus(){
		return ifStatus.compareTo("complete") == 0;
	}

	/**
		* Print the declaration
		* @return declaration as a string
		*/
	public String toString(){
		String str="";
		str= "Var "+name+":"+type;

		return str;
	}
}
