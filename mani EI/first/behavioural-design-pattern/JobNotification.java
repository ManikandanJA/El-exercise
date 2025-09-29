interface Observer {
    void update(String job);
}

class Candidate implements Observer {
    String name;
    Candidate(String n){name=n;}
    public void update(String job){
        System.out.println(name+" got job notification: "+job);
    }
}

class JobPortal {
    Observer o;
    void add(Observer o){this.o=o;}
    void notifyJob(String job){o.update(job);}
}

public class JobNotification {
    public static void main(String[] args){
        JobPortal portal = new JobPortal();
        portal.add(new Candidate("Kumar"));
        portal.notifyJob("Software Developer Position Available!");
    }
}
