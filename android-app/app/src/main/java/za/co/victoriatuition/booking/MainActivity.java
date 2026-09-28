package za.co.victoriatuition.booking;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.InputType;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    private static final int NAVY = Color.rgb(15,23,42);
    private static final int ORANGE = Color.rgb(229,77,46);
    private static final int GREEN = Color.rgb(34,197,94);
    private static final int BG = Color.rgb(248,250,252);
    private static final int MUTED = Color.rgb(100,116,139);
    private static final int LINE = Color.rgb(226,232,240);

    private SharedPreferences prefs;
    private LinearLayout root;
    private LinearLayout body;
    private String role = "";
    private String username = "";
    private JSONObject data;

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(NAVY);
        prefs = getSharedPreferences("vta_native_portal", MODE_PRIVATE);
        seedData();
        showWelcome();
    }

    private void seedData() {
        if (prefs.contains("data")) {
            try { data = new JSONObject(prefs.getString("data","{}")); return; } catch(Exception ignored){}
        }
        try {
            data = new JSONObject();
            JSONArray students = new JSONArray();
            JSONObject mitchell = new JSONObject();
            mitchell.put("username","mitchell.vta");
            mitchell.put("password","MitchellVTA26!");
            mitchell.put("name","Mitchell");
            mitchell.put("grade","Grade 9");
            mitchell.put("curriculum","IEB");
            mitchell.put("subjects","Natural Sciences");
            mitchell.put("planMonth","October 2026");
            mitchell.put("lessonTarget",4);
            mitchell.put("planGoal","Build exam confidence by understanding the science, not memorising answers.");
            mitchell.put("topics",new JSONArray(Arrays.asList(
                "Scientific Method & Variables",
                "Forces & Motion",
                "Electric Cells & Circuits",
                "Magnetism & Electrostatics"
            )));
            mitchell.put("lessons",new JSONArray());
            mitchell.put("resources",new JSONArray());
            mitchell.put("messages",new JSONArray());
            mitchell.put("bookings",new JSONArray());
            students.put(mitchell);
            data.put("students",students);

            JSONArray announcements = new JSONArray();
            JSONObject a = new JSONObject();
            a.put("title","Welcome to your student portal");
            a.put("message","Use this app to check your plan, request lessons, open resources and message Victoria.");
            a.put("date","28 Sep 2026");
            announcements.put(a);
            data.put("announcements",announcements);
            save();
        } catch(Exception ignored){}
    }

    private void save() { prefs.edit().putString("data", data.toString()).apply(); }

    private void showWelcome() {
        role=""; username="";
        ScrollView scroll=new ScrollView(this);
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(20),dp(28),dp(20),dp(28)); root.setBackgroundColor(BG);
        scroll.addView(root);
        setContentView(scroll);

        LinearLayout logo=row();
        TextView mark=text("VTA",22,NAVY,true); mark.setGravity(Gravity.CENTER);
        mark.setBackground(round(Color.WHITE,16,1,LINE)); mark.setPadding(dp(14),dp(14),dp(14),dp(14));
        logo.addView(mark,new LinearLayout.LayoutParams(dp(62),dp(62)));
        LinearLayout brand=col(); brand.setPadding(dp(12),0,0,0);
        brand.addView(text("Victoria Tuition Academy",20,NAVY,true));
        brand.addView(text("Better Understanding. Better Results.",12,MUTED,false));
        logo.addView(brand,new LinearLayout.LayoutParams(0,ViewGroup.LayoutParams.WRAP_CONTENT,1));
        root.addView(logo);

        Space sp=new Space(this); root.addView(sp,new LinearLayout.LayoutParams(1,dp(44)));
        root.addView(text("Welcome",36,NAVY,true));
        TextView intro=text("Choose how you’re entering the app.",15,MUTED,false); intro.setPadding(0,dp(8),0,dp(20)); root.addView(intro);

        addRoleCard("Student","View your plan, lessons, resources and messages.","STUDENT");
        addRoleCard("Parent","See the learner’s plan, bookings and announcements.","PARENT");
        addRoleCard("Admin","Manage students, lessons, resources and communication.","ADMIN");
    }

    private void addRoleCard(String title,String sub,String value){
        LinearLayout c=col(); c.setPadding(dp(18),dp(18),dp(18),dp(18)); c.setBackground(round(Color.WHITE,18,1,LINE));
        c.addView(text(title,18,NAVY,true)); TextView s=text(sub,13,MUTED,false); s.setPadding(0,dp(4),0,0); c.addView(s);
        c.setOnClickListener(v->{role=value;showLogin();});
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT); lp.setMargins(0,0,0,dp(12)); root.addView(c,lp);
    }

    private void showLogin(){
        ScrollView scroll=new ScrollView(this);
        root=col(); root.setPadding(dp(20),dp(28),dp(20),dp(28)); root.setBackgroundColor(BG); scroll.addView(root); setContentView(scroll);
        Button back=button("← Back",Color.TRANSPARENT,NAVY); back.setOnClickListener(v->showWelcome()); root.addView(back);
        root.addView(text(roleLabel()+" login",32,NAVY,true));
        TextView sub=text("Enter the username and password for this account.",14,MUTED,false); sub.setPadding(0,dp(8),0,dp(22)); root.addView(sub);

        EditText user=input("Username",false); root.addView(user);
        EditText pass=input("Password",true); LinearLayout.LayoutParams pLP=new LinearLayout.LayoutParams(-1,-2); pLP.setMargins(0,dp(12),0,dp(16)); root.addView(pass,pLP);
        TextView error=text("",12,Color.rgb(185,28,28),true); root.addView(error);

        Button login=button("Sign in",ORANGE,Color.WHITE); root.addView(login);
        TextView hint=text(role.equals("ADMIN")?"Admin username: victoria.admin":"Mitchell username: mitchell.vta",12,MUTED,false); hint.setPadding(0,dp(14),0,0); root.addView(hint);

        login.setOnClickListener(v->{
            String u=user.getText().toString().trim();
            String p=pass.getText().toString();
            if(role.equals("ADMIN")){
                if(u.equals("victoria.admin") && p.equals("VTA-Admin26!")){username=u;showDashboard();}
                else error.setText("Incorrect admin login.");
            } else {
                JSONObject s=findStudent(u);
                if(s!=null && p.equals(s.optString("password"))){username=u;showDashboard();}
                else error.setText("Incorrect student login.");
            }
        });
    }

    private void showDashboard(){
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setBackgroundColor(BG);
        LinearLayout header=row(); header.setGravity(Gravity.CENTER_VERTICAL); header.setPadding(dp(18),dp(16),dp(18),dp(16)); header.setBackgroundColor(NAVY);
        LinearLayout titleBox=col(); titleBox.addView(text("Victoria Tuition Academy",17,Color.WHITE,true));
        titleBox.addView(text(roleLabel()+" portal",11,Color.rgb(203,213,225),false));
        header.addView(titleBox,new LinearLayout.LayoutParams(0,-2,1));
        Button logout=button("Log out",Color.rgb(30,41,59),Color.WHITE); logout.setOnClickListener(v->showWelcome()); header.addView(logout);
        root.addView(header);

        ScrollView scroll=new ScrollView(this); body=col(); body.setPadding(dp(16),dp(16),dp(16),dp(92)); scroll.addView(body);
        root.addView(scroll,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout nav=row(); nav.setPadding(dp(8),dp(8),dp(8),dp(8)); nav.setBackgroundColor(Color.WHITE);
        String[] labels=role.equals("ADMIN")?new String[]{"Home","Students","Lessons","Resources","Messages"}:new String[]{"Home","Plan","Lessons","Resources","Messages"};
        for(String label:labels){Button b=button(label,Color.WHITE,NAVY); b.setTextSize(11); b.setOnClickListener(v->openTab(label)); nav.addView(b,new LinearLayout.LayoutParams(0,dp(54),1));}
        root.addView(nav);
        setContentView(root);
        openTab("Home");
    }

    private void openTab(String tab){
        body.removeAllViews();
        if(role.equals("ADMIN")) {
            if(tab.equals("Students")) adminStudents();
            else if(tab.equals("Lessons")) adminLessons();
            else if(tab.equals("Resources")) adminResources();
            else if(tab.equals("Messages")) adminMessages();
            else adminHome();
        } else {
            JSONObject s=findStudent(username);
            if(s==null){body.addView(text("Student record not found.",16,NAVY,true));return;}
            if(tab.equals("Plan")) studentPlan(s);
            else if(tab.equals("Lessons")) studentLessons(s);
            else if(tab.equals("Resources")) studentResources(s);
            else if(tab.equals("Messages")) studentMessages(s);
            else studentHome(s);
        }
    }

    private void adminHome(){
        body.addView(text("Admin dashboard",30,NAVY,true));
        body.addView(text("Manage the academy from one place.",14,MUTED,false));
        JSONArray students=data.optJSONArray("students");
        addStat("Students",students==null?0:students.length());
        int lessons=0, bookings=0;
        if(students!=null) for(int i=0;i<students.length();i++){JSONObject s=students.optJSONObject(i); lessons+=s.optJSONArray("lessons").length(); bookings+=s.optJSONArray("bookings").length();}
        addStat("Lessons",lessons); addStat("Booking requests",bookings);
        addSectionTitle("Quick actions");
        addAction("Create student account",v->dialogCreateStudent());
        addAction("Post announcement",v->dialogAnnouncement());
        addAction("Add lesson",v->dialogAddLesson(null));
        addAction("Add resource",v->dialogAddResource(null));
    }

    private void adminStudents(){
        body.addView(text("Students",30,NAVY,true));
        body.addView(text("Open a learner to edit their plan or credentials.",14,MUTED,false));
        addAction("+ Create student account",v->dialogCreateStudent());
        JSONArray a=data.optJSONArray("students");
        for(int i=0;i<a.length();i++){
            JSONObject s=a.optJSONObject(i);
            LinearLayout card=card();
            card.addView(text(s.optString("name"),18,NAVY,true));
            card.addView(text(s.optString("grade")+" · "+s.optString("curriculum")+" · "+s.optString("subjects"),12,MUTED,false));
            card.addView(text("Username: "+s.optString("username"),12,MUTED,false));
            Button edit=button("Edit plan",ORANGE,Color.WHITE); edit.setOnClickListener(v->dialogEditPlan(s)); card.addView(edit);
            body.addView(card,marginBottom(12));
        }
    }

    private void adminLessons(){
        body.addView(text("Lessons",30,NAVY,true));
        addAction("+ Add lesson",v->dialogAddLesson(null));
        JSONArray students=data.optJSONArray("students");
        for(int i=0;i<students.length();i++){
            JSONObject s=students.optJSONObject(i); JSONArray arr=s.optJSONArray("lessons");
            for(int j=0;j<arr.length();j++){
                JSONObject l=arr.optJSONObject(j); LinearLayout c=card();
                c.addView(text(s.optString("name")+" · "+l.optString("topic"),16,NAVY,true));
                c.addView(text(l.optString("date")+" · "+l.optString("subject")+" · "+l.optString("status"),12,MUTED,false));
                body.addView(c,marginBottom(10));
            }
        }
    }

    private void adminResources(){
        body.addView(text("Resources",30,NAVY,true)); addAction("+ Add resource",v->dialogAddResource(null));
        JSONArray students=data.optJSONArray("students");
        for(int i=0;i<students.length();i++){
            JSONObject s=students.optJSONObject(i); JSONArray arr=s.optJSONArray("resources");
            for(int j=0;j<arr.length();j++){
                JSONObject r=arr.optJSONObject(j); LinearLayout c=card();
                c.addView(text(s.optString("name")+" · "+r.optString("title"),16,NAVY,true));
                c.addView(text(r.optString("type")+" · "+r.optString("url"),12,MUTED,false)); body.addView(c,marginBottom(10));
            }
        }
    }

    private void adminMessages(){
        body.addView(text("Messages & announcements",30,NAVY,true));
        addAction("+ Post announcement",v->dialogAnnouncement());
        JSONArray anns=data.optJSONArray("announcements");
        for(int i=anns.length()-1;i>=0;i--){JSONObject a=anns.optJSONObject(i); LinearLayout c=card(); c.addView(text(a.optString("title"),16,NAVY,true)); c.addView(text(a.optString("message"),13,MUTED,false)); body.addView(c,marginBottom(10));}
        JSONArray students=data.optJSONArray("students");
        for(int i=0;i<students.length();i++){JSONObject s=students.optJSONObject(i); JSONArray m=s.optJSONArray("messages"); for(int j=m.length()-1;j>=0;j--){JSONObject msg=m.optJSONObject(j); LinearLayout c=card(); c.addView(text(s.optString("name")+" · "+msg.optString("from"),14,NAVY,true)); c.addView(text(msg.optString("text"),13,MUTED,false)); body.addView(c,marginBottom(8));}}
    }

    private void studentHome(JSONObject s){
        body.addView(text("Hi, "+s.optString("name"),30,NAVY,true));
        body.addView(text(s.optString("grade")+" · "+s.optString("curriculum")+" · "+s.optString("subjects"),13,MUTED,false));
        LinearLayout plan=card(); plan.addView(text(s.optString("planMonth")+" plan",17,NAVY,true)); plan.addView(text(s.optInt("lessonTarget")+" planned lessons",28,ORANGE,true)); plan.addView(text(s.optString("planGoal"),13,MUTED,false)); body.addView(plan,marginTopBottom(16,10));
        addSectionTitle("Quick actions");
        addAction("Request a lesson",v->dialogBooking(s));
        addAction("Message Victoria",v->dialogMessage(s));
        addSectionTitle("Announcements");
        JSONArray anns=data.optJSONArray("announcements");
        for(int i=anns.length()-1;i>=0;i--){JSONObject a=anns.optJSONObject(i); LinearLayout c=card(); c.addView(text(a.optString("title"),16,NAVY,true)); c.addView(text(a.optString("message"),13,MUTED,false)); c.addView(text(a.optString("date"),11,MUTED,false)); body.addView(c,marginBottom(10));}
    }

    private void studentPlan(JSONObject s){
        body.addView(text("My monthly plan",30,NAVY,true));
        body.addView(text(s.optString("planMonth"),14,MUTED,false));
        addStat("Planned lessons",s.optInt("lessonTarget"));
        LinearLayout goal=card(); goal.addView(text("Goal",15,NAVY,true)); goal.addView(text(s.optString("planGoal"),13,MUTED,false)); body.addView(goal,marginBottom(12));
        addSectionTitle("Focus topics");
        JSONArray topics=s.optJSONArray("topics");
        for(int i=0;i<topics.length();i++){LinearLayout c=card(); c.addView(text((i+1)+". "+topics.optString(i),15,NAVY,true)); body.addView(c,marginBottom(8));}
    }

    private void studentLessons(JSONObject s){
        body.addView(text("My lessons",30,NAVY,true));
        addAction("+ Request a lesson",v->dialogBooking(s));
        JSONArray bookings=s.optJSONArray("bookings");
        if(bookings.length()>0){addSectionTitle("Requests"); for(int i=bookings.length()-1;i>=0;i--){JSONObject b=bookings.optJSONObject(i); LinearLayout c=card(); c.addView(text(b.optString("date")+" · "+b.optString("duration"),15,NAVY,true)); c.addView(text(b.optString("topic"),13,MUTED,false)); c.addView(text("Status: "+b.optString("status"),12,ORANGE,true)); body.addView(c,marginBottom(8));}}
        addSectionTitle("Scheduled");
        JSONArray arr=s.optJSONArray("lessons");
        if(arr.length()==0) body.addView(text("No lessons added yet.",13,MUTED,false));
        for(int i=0;i<arr.length();i++){JSONObject l=arr.optJSONObject(i); LinearLayout c=card(); c.addView(text(l.optString("topic"),16,NAVY,true)); c.addView(text(l.optString("date")+" · "+l.optString("subject"),13,MUTED,false)); c.addView(text(l.optString("status"),12,GREEN,true)); body.addView(c,marginBottom(10));}
    }

    private void studentResources(JSONObject s){
        body.addView(text("My resources",30,NAVY,true));
        JSONArray arr=s.optJSONArray("resources");
        if(arr.length()==0) body.addView(text("Victoria hasn’t added resources yet.",13,MUTED,false));
        for(int i=0;i<arr.length();i++){JSONObject r=arr.optJSONObject(i); LinearLayout c=card(); c.addView(text(r.optString("title"),16,NAVY,true)); c.addView(text(r.optString("type"),12,ORANGE,true)); c.addView(text(r.optString("description"),13,MUTED,false)); TextView u=text(r.optString("url"),12,Color.rgb(37,99,235),false); c.addView(u); body.addView(c,marginBottom(10));}
    }

    private void studentMessages(JSONObject s){
        body.addView(text("Messages",30,NAVY,true)); addAction("+ Message Victoria",v->dialogMessage(s));
        JSONArray arr=s.optJSONArray("messages");
        for(int i=arr.length()-1;i>=0;i--){JSONObject m=arr.optJSONObject(i); LinearLayout c=card(); c.addView(text(m.optString("from"),14,NAVY,true)); c.addView(text(m.optString("text"),13,MUTED,false)); c.addView(text(m.optString("date"),11,MUTED,false)); body.addView(c,marginBottom(8));}
    }

    private void dialogCreateStudent(){
        LinearLayout box=formBox(); EditText n=input("Student name",false), g=input("Grade",false), cur=input("Curriculum",false), sub=input("Subjects",false), u=input("Username",false), p=input("Temporary password",false);
        for(EditText e:new EditText[]{n,g,cur,sub,u,p}) box.addView(e,marginBottom(9));
        new AlertDialog.Builder(this).setTitle("Create student account").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Create",(d,w)->{
            try{
                JSONObject s=new JSONObject(); s.put("name",n.getText().toString()); s.put("grade",g.getText().toString()); s.put("curriculum",cur.getText().toString()); s.put("subjects",sub.getText().toString()); s.put("username",u.getText().toString()); s.put("password",p.getText().toString()); s.put("planMonth","Not set"); s.put("lessonTarget",0); s.put("planGoal",""); s.put("topics",new JSONArray()); s.put("lessons",new JSONArray()); s.put("resources",new JSONArray()); s.put("messages",new JSONArray()); s.put("bookings",new JSONArray()); data.optJSONArray("students").put(s); save(); adminStudents();
            }catch(Exception ignored){}
        }).show();
    }

    private void dialogEditPlan(JSONObject s){
        LinearLayout box=formBox(); EditText month=input("Month",false); month.setText(s.optString("planMonth")); EditText count=input("Planned lessons",false); count.setInputType(InputType.TYPE_CLASS_NUMBER); count.setText(String.valueOf(s.optInt("lessonTarget"))); EditText goal=input("Monthly goal",false); goal.setText(s.optString("planGoal")); EditText topics=input("Focus topics, separated by commas",false);
        JSONArray ta=s.optJSONArray("topics"); List<String> ts=new ArrayList<>(); for(int i=0;i<ta.length();i++)ts.add(ta.optString(i)); topics.setText(android.text.TextUtils.join(", ",ts));
        for(EditText e:new EditText[]{month,count,goal,topics}) box.addView(e,marginBottom(9));
        new AlertDialog.Builder(this).setTitle("Edit "+s.optString("name")+" plan").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{try{s.put("planMonth",month.getText().toString());s.put("lessonTarget",Integer.parseInt(count.getText().toString().trim().isEmpty()?"0":count.getText().toString().trim()));s.put("planGoal",goal.getText().toString());JSONArray ar=new JSONArray();for(String x:topics.getText().toString().split(","))if(!x.trim().isEmpty())ar.put(x.trim());s.put("topics",ar);save();adminStudents();}catch(Exception ignored){}}).show();
    }

    private void dialogAddLesson(JSONObject chosen){
        JSONObject student=chosen!=null?chosen:firstStudent();
        if(student==null)return;
        LinearLayout box=formBox(); EditText date=input("Date & time",false), subject=input("Subject",false), topic=input("Topic",false), status=input("Status",false); status.setText("Planned");
        for(EditText e:new EditText[]{date,subject,topic,status})box.addView(e,marginBottom(9));
        new AlertDialog.Builder(this).setTitle("Add lesson for "+student.optString("name")).setView(box).setNegativeButton("Cancel",null).setPositiveButton("Add",(d,w)->{try{JSONObject l=new JSONObject();l.put("date",date.getText().toString());l.put("subject",subject.getText().toString());l.put("topic",topic.getText().toString());l.put("status",status.getText().toString());student.optJSONArray("lessons").put(l);save();adminLessons();}catch(Exception ignored){}}).show();
    }

    private void dialogAddResource(JSONObject chosen){
        JSONObject student=chosen!=null?chosen:firstStudent(); if(student==null)return;
        LinearLayout box=formBox(); EditText title=input("Title",false), type=input("Type: Video / Slides / Notes / Worksheet",false), url=input("Link",false), desc=input("Description",false);
        for(EditText e:new EditText[]{title,type,url,desc})box.addView(e,marginBottom(9));
        new AlertDialog.Builder(this).setTitle("Add resource for "+student.optString("name")).setView(box).setNegativeButton("Cancel",null).setPositiveButton("Add",(d,w)->{try{JSONObject r=new JSONObject();r.put("title",title.getText().toString());r.put("type",type.getText().toString());r.put("url",url.getText().toString());r.put("description",desc.getText().toString());student.optJSONArray("resources").put(r);save();adminResources();}catch(Exception ignored){}}).show();
    }

    private void dialogAnnouncement(){
        LinearLayout box=formBox(); EditText title=input("Title",false), msg=input("Announcement",false); box.addView(title,marginBottom(9));box.addView(msg);
        new AlertDialog.Builder(this).setTitle("Post announcement").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Post",(d,w)->{try{JSONObject a=new JSONObject();a.put("title",title.getText().toString());a.put("message",msg.getText().toString());a.put("date",today());data.optJSONArray("announcements").put(a);save();adminMessages();}catch(Exception ignored){}}).show();
    }

    private void dialogBooking(JSONObject s){
        LinearLayout box=formBox(); EditText date=input("Preferred date & time",false), duration=input("Duration: 1 hour or 2 hours",false), topic=input("Topic / what you need help with",false);
        for(EditText e:new EditText[]{date,duration,topic})box.addView(e,marginBottom(9));
        new AlertDialog.Builder(this).setTitle("Request a lesson").setView(box).setMessage("This is a request. Victoria will confirm whether the time works.").setNegativeButton("Cancel",null).setPositiveButton("Send request",(d,w)->{try{JSONObject b=new JSONObject();b.put("date",date.getText().toString());b.put("duration",duration.getText().toString());b.put("topic",topic.getText().toString());b.put("status","Pending");s.optJSONArray("bookings").put(b);save();studentLessons(s);}catch(Exception ignored){}}).show();
    }

    private void dialogMessage(JSONObject s){
        EditText msg=input("Write your message",false);
        new AlertDialog.Builder(this).setTitle("Message Victoria").setView(msg).setNegativeButton("Cancel",null).setPositiveButton("Send",(d,w)->{try{JSONObject m=new JSONObject();m.put("from",s.optString("name"));m.put("text",msg.getText().toString());m.put("date",today());s.optJSONArray("messages").put(m);save();studentMessages(s);}catch(Exception ignored){}}).show();
    }

    private JSONObject findStudent(String u){
        JSONArray a=data.optJSONArray("students"); if(a==null)return null;
        for(int i=0;i<a.length();i++){JSONObject s=a.optJSONObject(i); if(s!=null&&u.equalsIgnoreCase(s.optString("username")))return s;}
        return null;
    }
    private JSONObject firstStudent(){JSONArray a=data.optJSONArray("students");return a!=null&&a.length()>0?a.optJSONObject(0):null;}

    private void addStat(String label,int value){LinearLayout c=card();c.addView(text(String.valueOf(value),30,ORANGE,true));c.addView(text(label,12,MUTED,false));body.addView(c,marginTopBottom(12,0));}
    private void addSectionTitle(String s){TextView t=text(s,17,NAVY,true);t.setPadding(0,dp(18),0,dp(10));body.addView(t);}
    private void addAction(String s,View.OnClickListener l){Button b=button(s,Color.WHITE,NAVY);b.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);b.setPadding(dp(16),0,dp(16),0);b.setBackground(round(Color.WHITE,15,1,LINE));b.setOnClickListener(l);body.addView(b,marginBottom(10));}
    private LinearLayout card(){LinearLayout c=col();c.setPadding(dp(16),dp(16),dp(16),dp(16));c.setBackground(round(Color.WHITE,17,1,LINE));return c;}
    private LinearLayout formBox(){LinearLayout b=col();b.setPadding(dp(4),dp(8),dp(4),0);return b;}
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);return l;}
    private LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private TextView text(String s,float size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setLineSpacing(0,1.08f);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private EditText input(String hint,boolean password){EditText e=new EditText(this);e.setHint(hint);e.setTextColor(NAVY);e.setHintTextColor(Color.rgb(148,163,184));e.setTextSize(15);e.setPadding(dp(14),dp(12),dp(14),dp(12));e.setBackground(round(Color.WHITE,14,1,Color.rgb(203,213,225)));if(password)e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);return e;}
    private Button button(String s,int bg,int fg){Button b=new Button(this);b.setText(s);b.setTextColor(fg);b.setTextSize(14);b.setAllCaps(false);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setBackground(round(bg,14,0,0));return b;}
    private GradientDrawable round(int color,int radius,int stroke,int strokeColor){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));if(stroke>0)g.setStroke(dp(stroke),strokeColor);return g;}
    private LinearLayout.LayoutParams marginBottom(int m){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,dp(m));return p;}
    private LinearLayout.LayoutParams marginTopBottom(int t,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(t),0,dp(b));return p;}
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private String roleLabel(){return role.equals("ADMIN")?"Admin":role.equals("PARENT")?"Parent":"Student";}
    private String today(){return new SimpleDateFormat("dd MMM yyyy",Locale.getDefault()).format(new Date());}
}
