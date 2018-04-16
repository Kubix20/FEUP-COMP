

public class Declaration {
	
	public String name;
	public String type;
	public boolean init;
	public int value;
	public int values[];
	public int size;
	public int local;
	
	
	public Declaration(){
		init = false;
		local = -1;
	}
	
	public Declaration(String name){
		this.name = name;
		init = false;
		local = -1;
	}
	
	public Declaration(String name, String type){
		this.name = name;
		this.type = type;
		init = false;
		local = -1;
	}

	
	public Declaration(String type, int local){
		name = "";
		this.type = type;
		init = false;
		this.local = local;
	}
	
	public void initArray (int size){
		if(!init)
		{
			init = true;
			this.size = size;
			values = new int[size];
			type = "array";
		}
	}
	
	public void initInt (int value){
		if(!init)
		{
			init = true;
			this.value = value;
			type = "inteiro";
		}
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
