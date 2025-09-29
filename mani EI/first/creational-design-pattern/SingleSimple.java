class DB {
    private static DB obj;
    private DB(){System.out.println("DB Created");}
    public static DB get(){ if(obj==null)obj=new DB(); return obj;}
}

public class SingleSimple {
    public static void main(String[] a){
        DB d1=DB.get();
        DB d2=DB.get();
        System.out.println(d1==d2); // true
    }
}
