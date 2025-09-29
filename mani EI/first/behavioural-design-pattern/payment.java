interface Pay { void pay(int amt); }
class Card implements Pay{public void pay(int a){System.out.println("Card pay "+a);}}
class UPI implements Pay{public void pay(int a){System.out.println("UPI pay "+a);}}

class Shop {
    Pay p;
    void set(Pay p){this.p=p;}
    void checkout(int amt){p.pay(amt);}
}

public class payment {
    public static void main(String[] a){
        Shop s=new Shop();
        s.set(new Card()); s.checkout(100);
        s.set(new UPI()); s.checkout(200);
    }
}
