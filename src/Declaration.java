

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
	
	public Declaration(String n){
		name = n;
		init = false;
		local = -1;
	}
	
	public Declaration(String n, String t){
		name = n;
		type = t;
		init = false;
		local = -1;
	}

	
	public Declaration(String t, int l){
		name = "";
		type = t;
		init = false;
		local = l;
	}
	
	public void initArray (int s){
		if(!init)
		{
			init = true;
			size = s;
			values = new int[s];
			type = "array";
		}
	}
	
	public void initInt (int val){
		if(!init)
		{
			init = true;
			value = val;
			type = "inteiro";
		}
	}
	
	public String toString(){
		String str="";

		if(!init)
			str= "Variavel " + name + " declarada";
		else
		if(type.compareTo("inteiro") == 0 && init)
				str=name + " inicializado com o valor " + value;
		else if (type.compareTo("array") == 0 && init)
				str=name + " inicializado com tamanho " + size;
		
		return str;
	}
}
