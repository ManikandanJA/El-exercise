class DVD { void play(){System.out.println("DVD play");} }
class Projector { void on(){System.out.println("Projector on");} }

class Theater {
    DVD d; Projector p;
    Theater(){d=new DVD();p=new Projector();}
    void watch(){d.play();p.on();System.out.println("Movie Time!");}
}

public class FacadeSimple {
    public static void main(String[] a){
        Theater t=new Theater();
        t.watch();
    }
}
