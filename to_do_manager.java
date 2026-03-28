import java.util.*;
import java.io.*;

class Task {

    int userId,id,deadline;
    String name,priority;
    Task next;

    Task(int userId,int id,String name,String priority,int deadline){
        this.userId=userId;
        this.id=id;
        this.name=name;
        this.priority=priority;
        this.deadline=deadline;
        next=null;
    }
}

class TaskManager{

    Task activehead=null;
    Task highhead=null;
    Task completedhead=null;

    Stack<Task> undoStack=new Stack<>();
    Queue<Task> executionQueue=new LinkedList<>();

    int currentUserId;
    int idGenerator=101;

    TaskManager(int userId){
        currentUserId=userId;
    }

    // ================= FILE LOADING =================

    void loadfile(){

        try{

            File f=new File("tasks.txt");

            if(!f.exists())
                return;

            FileReader fr=new FileReader(f);

            String line="";
            int ch;

            while((ch=fr.read())!=-1){

                if(ch!='\n')
                    line+=(char)ch;

                else{

                    String data[]=line.split(",");

                    int userId=Integer.parseInt(data[0]);

                    if(userId==currentUserId){

                        int id=Integer.parseInt(data[1]);
                        String name=data[2];
                        String priority=data[3];
                        int deadline=Integer.parseInt(data[4]);
                        String status=data[5];

                        Task node=new Task(userId,id,name,priority,deadline);

                        if(status.equals("ACTIVE")){

                            if(activehead==null)
                                activehead=node;

                            else{
                                Task temp=activehead;
                                while(temp.next!=null)
                                    temp=temp.next;
                                temp.next=node;
                            }

                            if(priority.equals("high")){

                                Task highNode=new Task(userId,id,name,priority,deadline);

                                if(highhead==null)
                                    highhead=highNode;

                                else{
                                    Task t=highhead;
                                    while(t.next!=null)
                                        t=t.next;
                                    t.next=highNode;
                                }
                            }
                        }

                        else{

                            node.next=completedhead;
                            completedhead=node;
                        }

                        if(id>=idGenerator)
                            idGenerator=id+1;
                    }

                    line="";
                }
            }

            fr.close();

        }catch(Exception e){
            System.out.println("Error loading file");
        }
    }

    // ================= FILE SAVING =================

    void savefile(){

        try{

            File f=new File("tasks.txt");

            String olddata="";

            if(f.exists()){

                FileReader fr=new FileReader(f);
                int ch;

                while((ch=fr.read())!=-1)
                    olddata+=(char)ch;

                fr.close();
            }

            FileWriter fw=new FileWriter("tasks.txt");

            String lines[]=olddata.split("\n");

            for(int i=0;i<lines.length;i++){

                if(lines[i].length()==0)
                    continue;

                if(!lines[i].startsWith(currentUserId+","))
                    fw.write(lines[i]+"\n");
            }

            Task temp=activehead;

            while(temp!=null){

                fw.write(temp.userId+","+temp.id+","+temp.name+","+temp.priority+","+temp.deadline+",ACTIVE\n");
                temp=temp.next;
            }

            temp=completedhead;

            while(temp!=null){

                fw.write(temp.userId+","+temp.id+","+temp.name+","+temp.priority+","+temp.deadline+",COMPLETED\n");
                temp=temp.next;
            }

            fw.close();

        }catch(Exception e){
            System.out.println("Error saving file");
        }
    }

    // ================= ADD TASK =================

    void addTask(){

        Scanner sc=new Scanner(System.in);

        System.out.print("Enter task name: ");
        String name=sc.next();

        System.out.print("Enter priority(high/medium/low): ");
        String priority=sc.next();

        System.out.print("Enter deadline: ");
        int deadline=sc.nextInt();

        Task node=new Task(currentUserId,idGenerator++,name,priority,deadline);

        if(activehead==null)
            activehead=node;

        else{

            Task temp=activehead;

            while(temp.next!=null)
                temp=temp.next;

            temp.next=node;
        }

        if(priority.equals("high")){

            Task h=new Task(currentUserId,node.id,name,priority,deadline);

            if(highhead==null)
                highhead=h;

            else{

                Task t=highhead;

                while(t.next!=null)
                    t=t.next;

                t.next=h;
            }
        }

        savefile();

        System.out.println("Task added");
    }

    // ================= COMPLETE TASK =================

    void completeTask(int id){

        Task prev=null;
        Task curr=activehead;

        while(curr!=null && curr.id!=id){

            prev=curr;
            curr=curr.next;
        }

        if(curr==null){
            System.out.println("Task not found");
            return;
        }

        if(prev==null)
            activehead=curr.next;

        else
            prev.next=curr.next;

        undoStack.push(curr);

        curr.next=completedhead;
        completedhead=curr;

        savefile();

        System.out.println("Task completed");
    }

    // ================= UNDO =================

    void undoComplete(){

        if(undoStack.isEmpty()){

            System.out.println("Nothing to undo");
            return;
        }

        Task node=undoStack.pop();

        node.next=activehead;
        activehead=node;

        System.out.println("Undo successful");
    }

    // ================= SEARCH =================

    void searchByID(int id){

    ArrayList<Task> list = new ArrayList<>();
    Task temp = activehead;

    while(temp != null){
        list.add(temp);
        temp = temp.next;
    }

    Collections.sort(list, (a,b) -> Integer.compare(a.id, b.id));

    int left = 0, right = list.size() - 1;

    while(left <= right){

        int mid = (left + right) / 2;

        if(list.get(mid).id == id){
            Task t = list.get(mid);
            System.out.println("Found " + t.name + " deadline " + t.deadline);
            return;
        }

        else if(list.get(mid).id < id)
            left = mid + 1;

        else
            right = mid - 1;
    }

    System.out.println("Task not found");
}

    void searchByName(String name){

    ArrayList<Task> list = new ArrayList<>();
    Task temp = activehead;

    while(temp != null){
        list.add(temp);
        temp = temp.next;
    }

    Collections.sort(list, (a,b) -> a.name.compareTo(b.name));

    int left = 0, right = list.size() - 1;

    while(left <= right){

        int mid = (left + right) / 2;

        int cmp = list.get(mid).name.compareTo(name);

        if(cmp == 0){
            Task t = list.get(mid);
            System.out.println("Found ID " + t.id + " deadline " + t.deadline);
            return;
        }

        else if(cmp < 0)
            left = mid + 1;

        else
            right = mid - 1;
    }

    System.out.println("Task not found");
}

  void searchByPriority(String p){

    ArrayList<Task> list = new ArrayList<>();
    Task temp = activehead;

    while(temp != null){
        list.add(temp);
        temp = temp.next;
    }

    Collections.sort(list, (a,b) -> a.priority.compareTo(b.priority));

    int left = 0, right = list.size() - 1;
    boolean found = false;

    while(left <= right){

        int mid = (left + right) / 2;

        int cmp = list.get(mid).priority.compareTo(p);

        if(cmp == 0){

            // expand left
            int i = mid;
            while(i >= 0 && list.get(i).priority.equals(p)){
                System.out.println("ID:"+list.get(i).id+" Task:"+list.get(i).name);
                i--;
            }

            // expand right
            i = mid + 1;
            while(i < list.size() && list.get(i).priority.equals(p)){
                System.out.println("ID:"+list.get(i).id+" Task:"+list.get(i).name);
                i++;
            }

            found = true;
            break;
        }

        else if(cmp < 0)
            left = mid + 1;

        else
            right = mid - 1;
    }

    if(!found)
        System.out.println("No tasks found");
}

    // ================= PRIORITY SORT =================

    int priorityValue(String p){

        if(p.equals("high")) return 1;
        if(p.equals("medium")) return 2;
        return 3;
    }

    void sortByPriority(){

        Task i=activehead;

        while(i!=null){

            Task j=i.next;

            while(j!=null){

                if(priorityValue(j.priority)<priorityValue(i.priority)){

                    int id=i.id;
                    String name=i.name;
                    String p=i.priority;
                    int d=i.deadline;

                    i.id=j.id;
                    i.name=j.name;
                    i.priority=j.priority;
                    i.deadline=j.deadline;

                    j.id=id;
                    j.name=name;
                    j.priority=p;
                    j.deadline=d;
                }

                j=j.next;
            }

            i=i.next;
        }
    }

    // ================= SCHEDULING =================

    void scheduleTasks(){

        sortByPriority();

        Task temp=activehead;

        while(temp!=null){

            executionQueue.add(temp);
            temp=temp.next;
        }

        System.out.println("Tasks scheduled");
    }

    void executeTasks(){

        if(executionQueue.isEmpty()){

            System.out.println("No tasks scheduled");
            return;
        }

        System.out.println("Execution order:");

        while(!executionQueue.isEmpty()){

            Task t=executionQueue.poll();

            System.out.println("Task:"+t.name+" Priority:"+t.priority+" Deadline:"+t.deadline);
        }
    }

    // ================= DISPLAY =================

    void display(Task head,String title){

        System.out.println("\n---"+title+"---");

        Task temp=head;

        if(temp==null){

            System.out.println("No tasks");
            return;
        }

        while(temp!=null){

            System.out.println("ID:"+temp.id+" Task:"+temp.name+" Priority:"+temp.priority+" Deadline:"+temp.deadline);
            temp=temp.next;
        }
    }
}

public class to_do_manager{

    static int generateUser(){

        int lastId=0;

        try{

            File f=new File("users.txt");

            if(f.exists()){

                FileReader fr=new FileReader(f);

                String num="";
                int ch;

                while((ch=fr.read())!=-1){

                    if(ch!='\n')
                        num+=(char)ch;

                    else{

                        lastId=Integer.parseInt(num);
                        num="";
                    }
                }

                if(!num.equals(""))
                    lastId=Integer.parseInt(num);

                fr.close();
            }

            lastId++;

            FileWriter fw=new FileWriter("users.txt",true);
            fw.write(lastId+"\n");
            fw.close();

        }catch(Exception e){
            System.out.println("User file error");
        }

        return lastId;
    }

    static boolean validateUser(int id){

        try{

            File f=new File("users.txt");

            if(!f.exists())
                return false;

            FileReader fr=new FileReader(f);

            String num="";
            int ch;

            while((ch=fr.read())!=-1){

                if(ch!='\n')
                    num+=(char)ch;

                else{

                    int stored=Integer.parseInt(num);

                    if(stored==id){

                        fr.close();
                        return true;
                    }

                    num="";
                }
            }

            fr.close();

        }catch(Exception e){
            System.out.println("Error reading users file");
        }

        return false;
    }

    public static void main(String args[]){

        Scanner sc=new Scanner(System.in);

        int userId=0;

        System.out.println("1 New User");
        System.out.println("2 Existing User");

        int type=sc.nextInt();

        if(type==1){

            userId=generateUser();
            System.out.println("Your User ID:"+userId);
        }

        else{

            System.out.print("Enter User ID:");
            int id=sc.nextInt();

            if(validateUser(id))
                userId=id;

            else{
                System.out.println("User not found");
                return;
            }
        }

        TaskManager tm=new TaskManager(userId);

        tm.loadfile();

        String choice;

        do{

            System.out.println("\n1 Add Task");
            System.out.println("2 Show Active Tasks");
            System.out.println("3 Show High Priority Tasks");
            System.out.println("4 Show Completed Tasks");
            System.out.println("5 Search by ID");
            System.out.println("6 Search by Name");
            System.out.println("7 Search by Priority");
            System.out.println("8 Complete Task");
            System.out.println("9 Undo Complete");
            System.out.println("10 Schedule Tasks");
            System.out.println("11 Execute Tasks");

            int ch=sc.nextInt();

            switch(ch){

                case 1: tm.addTask(); break;
                case 2: tm.display(tm.activehead,"ACTIVE"); break;
                case 3: tm.display(tm.highhead,"HIGH PRIORITY"); break;
                case 4: tm.display(tm.completedhead,"COMPLETED"); break;
                case 5: tm.searchByID(sc.nextInt()); break;
                case 6: tm.searchByName(sc.next()); break;
                case 7: tm.searchByPriority(sc.next()); break;
                case 8: tm.completeTask(sc.nextInt()); break;
                case 9: tm.undoComplete(); break;
                case 10: tm.scheduleTasks(); break;
                case 11: tm.executeTasks(); break;
            }

            System.out.println("Continue? YES");
            choice=sc.next();

        }while(choice.equalsIgnoreCase("YES"));
    }
}