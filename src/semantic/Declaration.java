package semantic;

public class Declaration {

	public String name;
	public String type;
	public String access = "";
	public boolean sizeAccess = false;
	public String ifStatus = "";
	public boolean newIfBranch = false;
	public boolean global;
	public boolean init;
	public int value;
	public int size = -1;
	public int local = -1;

	public Declaration(){
		init = false;
	}

	public Declaration(String name){
		this.name = name;
		type = "undefined";
		init = false;
		global = false;
	}

	public Declaration(String name, String type, boolean global){
		this.name = name;
		this.type = type;
		init = false;
		this.global = global;
	}


	public Declaration(Declaration var){
		name = var.name;
		type = var.type;
		access = var.access;
		ifStatus = var.ifStatus;
		global = var.global;
		init = var.init;
		local = var.value;
	}

	public void init(String type){
		if(type.compareTo("integer") == 0)
			initInt();

		else if(type.compareTo("array") == 0)
			initArray();
	}

	public void initInt (){
		if(!init)
		{
			init = true;
			type = "integer";
		}
	}

	public void initInt (int value){
		if(!init)
		{
			init = true;
			this.value = value;
			type = "integer";
		}
	}

	public void initArray(){
		if(!init)
		{
			init = true;
			type = "array";
		}
	}

	public void initArray(int size){
		if(!init)
		{
			init = true;
			this.size = size;
			type = "array";
		}
	}

	public boolean isInitialized(){
		if(global)
			return true;
		else
			return init;
	}

	public boolean isInt(){
		return type.compareTo("integer") == 0;
	}

	public boolean isArray(){
		return type.compareTo("array") == 0;
	}

	public boolean isUndefined(){
		return type.compareTo("undefined") == 0;
	}

	public boolean intAccess(){
		return access.compareTo("integer") == 0;
	}

	public boolean arrayAccess(){
		return access.compareTo("array") == 0;
	}

	public boolean undefinedAccess(){
		return access.compareTo("undefined") == 0;
	}

	public boolean partialIfStatus(){
		return ifStatus.compareTo("partial") == 0;
	}

	public boolean incompatibleIfStatus(){
		return ifStatus.compareTo("incompatible") == 0;
	}

	public boolean completeIfStatus(){
		return ifStatus.compareTo("complete") == 0;
	}

	public String toString(){
		String str="";

		str= "Var "+name+":"+type;

		/*
		else
		if(type.compareTo("inteiro") == 0 && init)
				str=name + " inicializado com o valor " + value;
		else if (type.compareTo("array") == 0 && init)
				str=name + " inicializado com tamanho " + size;
		*/

		return str;
	}
}
