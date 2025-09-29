class OldC { void old(){System.out.println("Old charger");} }
interface NewC { void usb(); }

class Adapter implements NewC {
    OldC o; Adapter(OldC o){this.o=o;}
    public void usb(){o.old();}
}

public class AdapterSimple {
    public static void main(String[] a){
        NewC n=new Adapter(new OldC());
        n.usb();
    }
}
