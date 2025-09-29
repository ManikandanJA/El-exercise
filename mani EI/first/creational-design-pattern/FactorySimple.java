interface Shape { void draw(); }
class Circle implements Shape{public void draw(){System.out.println("Circle");}}
class Square implements Shape{public void draw(){System.out.println("Square");}}

class ShapeFactory {
    static Shape get(String t){
        if(t.equals("c")) return new Circle();
        else return new Square();
    }
}

public class FactorySimple {
    public static void main(String[] a){
        Shape s=ShapeFactory.get("c"); s.draw();
        Shape s2=ShapeFactory.get("s"); s2.draw();
    }
}
