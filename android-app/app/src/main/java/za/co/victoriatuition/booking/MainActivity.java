package za.co.victoriatuition.booking;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.database.Cursor;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.Settings;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    private static final int NAVY = Color.rgb(15,23,42);
    private static final int NAVY2 = Color.rgb(30,41,59);
    private static final int ORANGE = Color.rgb(229,77,46);
    private static final int ORANGE_SOFT = Color.rgb(255,244,240);
    private static final int GREEN = Color.rgb(34,197,94);
    private static final int GREEN_SOFT = Color.rgb(240,253,244);
    private static final int BLUE = Color.rgb(37,99,235);
    private static final int PURPLE = Color.rgb(124,58,237);
    private static final int BG = Color.rgb(246,248,252);
    private static final int MUTED = Color.rgb(100,116,139);
    private static final int LINE = Color.rgb(226,232,240);
    private static final int TEXT = Color.rgb(30,41,59);
    private static final int FILE_PICK = 9001;
    private static final int RATE = 150;
    private static final int DATA_VERSION = 6;

    private SharedPreferences prefs;
    private JSONObject data;
    private String role = "";
    private String username = "";
    private LinearLayout body;
    private JSONObject pendingResourceStudent;
    private String pendingResourceTitle = "";
    private String pendingResourceType = "File";

    @Override public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setStatusBarColor(NAVY);
        getWindow().setNavigationBarColor(Color.WHITE);
        prefs = getSharedPreferences("vta_portal_premium_v1", MODE_PRIVATE);
        seedData();
        createNotificationChannel();
        showWelcome();
    }

    private void seedData() {
        JSONObject old=null;
        if (prefs.contains("data")) {
            try { old=new JSONObject(prefs.getString("data","{}")); } catch(Exception ignored) {}
        }
        try {
            if(old!=null && old.optInt("_dataVersion",0)>=DATA_VERSION){
                data=old; ensureShape(); return;
            }
            JSONObject fresh=buildCanonicalData();
            if(old!=null) mergeUserContent(old,fresh);
            fresh.put("_dataVersion",DATA_VERSION);
            data=fresh;
            save();
        } catch(Exception e) {
            data=new JSONObject();
            try { data.put("students",new JSONArray()); data.put("announcements",new JSONArray()); data.put("schedule",new JSONArray()); data.put("payments",new JSONArray()); data.put("settings",new JSONObject()); } catch(Exception ignored){}
            save();
        }
    }

    private JSONObject buildCanonicalData() throws JSONException {
        JSONObject fresh=new JSONObject();
        JSONArray students=new JSONArray();

        JSONObject mitchell=student("mitchell.vta","MitchellVTA26!","Mitchell","Grade 9","IEB","Natural Sciences",4,
            "Prepare confidently for Grade 9 IEB exams by understanding the science and applying it to exam questions.",
            arr("Scientific Method & Variables","Forces & Motion","Electric Cells & Circuits","Magnetism & Electrostatics","Motion graphs"));
        addLesson(mitchell,"2026-09-12","Time not recorded",60,"Natural Sciences","Forces & Motion Masterclass","Completed","Verified lesson record. Focus included gravity/weight, magnetism, resultant forces and motion graphs.");
        addLesson(mitchell,"2026-09-20","Time not recorded",60,"Natural Sciences","Electric Circuits","Completed","Recorded lesson. Exact start time not retained.");
        addLesson(mitchell,"2026-09-22","Time not recorded",60,"Natural Sciences","Electric Cells as Energy Systems","Completed","Cells vs batteries, polarity, series/parallel voltage and investigation skills.");
        students.put(mitchell);

        JSONObject lateya=student("lateya.vta","LateyaVTA26!","Lateya","Grade 10","CAPS","Mathematics",4,
            "Move from relying on notes to independently solving Grade 10 exam questions.",
            arr("Functions & Graphs","Algebra","Trigonometry","Exam technique"));
        addLesson(lateya,"2026-09-19","Time not recorded",60,"Mathematics","Trigonometry","Completed","Recorded Grade 10 trigonometry lesson; exact start time not retained.");
        addLesson(lateya,"2026-09-28","17:00",60,"Mathematics","Functions","Confirmed","Confirmed Grade 10 Functions lesson.");
        students.put(lateya);

        JSONObject phelandi=student("phelandi.vta","PhelandiVTA26!","Phelandi","Grade 9","Not recorded","Natural Sciences / Physics · Mathematics",6,
            "Strengthen Physics/Natural Sciences and Mathematics through worked examples and exam-style practice.",
            arr("Factors affecting resistance","Equations of motion","Mathematics Term 1–3 revision","Circuit problem solving"));
        addLesson(phelandi,"2026-09-19","Time not recorded",120,"Natural Sciences / Physics","Physics / Natural Sciences lesson","Completed","Recorded two-hour lesson; exact topic and start time not retained.");
        addLesson(phelandi,"2026-09-22","14:00",120,"Physics / Natural Sciences","Factors Affecting Resistance","Completed","Verified 14:00–16:00 two-hour Physics lesson.");
        addLesson(phelandi,"2026-09-23","Time not recorded",60,"Physics / Natural Sciences","Physics follow-up","Completed","Recorded one-hour follow-up; exact start time/topic not retained.");
        addLesson(phelandi,"2026-09-29","12:00",120,"Mathematics + Physics","Maths + Physics revision","Planned","Confirmed planned two-hour lesson.");
        students.put(phelandi);

        JSONObject asimtusi=student("asimtusi.vta","AsimtusiVTA26!","Asimtusi","Grade 10","Not recorded","Physics",4,
            "Build confidence in Grade 10 Physics through concept mastery and exam-style application.",
            arr("Motion","Equations of motion","Graphs","Physics exam practice"));
        addLesson(asimtusi,"2026-09-23","Time not recorded",60,"Physics","Physics lesson","Completed","Recorded one-hour lesson; exact topic and start time not retained.");
        addLesson(asimtusi,"2026-09-30","09:00",120,"Physics","Grade 10 Physics","Planned","Confirmed planned two-hour lesson.");
        students.put(asimtusi);

        fresh.put("students",students);

        JSONArray announcements=new JSONArray();
        announcements.put(obj("title","Welcome to your VTA portal","message","Your lessons, plan, resources, booking requests and messages live in one place.","date","28 Sep 2026"));
        fresh.put("announcements",announcements);

        JSONArray schedule=new JSONArray();
        schedule.put(schedule("2026-09-28","17:00","18:00","Lateya — Grade 10 Maths: Functions","Students"));
        schedule.put(schedule("2026-09-29","12:00","14:00","Phelandi — Maths + Physics","Students"));
        schedule.put(schedule("2026-09-30","09:00","11:00","Asimtusi — Grade 10 Physics","Students"));
        fresh.put("schedule",schedule);

        JSONArray payments=new JSONArray();
        JSONObject tracey=new JSONObject();
        tracey.put("payer","Tracey"); tracey.put("date","2026-09-15"); tracey.put("amount",-1);
        tracey.put("note","Payment received on the 15th. Amount not entered because the exact amount is not verified in the app record.");
        payments.put(tracey); fresh.put("payments",payments);

        JSONObject settings=new JSONObject();
        settings.put("lessonFormat","Detailed cards");
        settings.put("resourceFormat","Preview cards");
        settings.put("slotInterval",30);
        fresh.put("settings",settings);
        return fresh;
    }

    private void mergeUserContent(JSONObject old,JSONObject fresh){
        try{
            JSONArray oldStudents=old.optJSONArray("students"), newStudents=fresh.optJSONArray("students");
            if(oldStudents!=null){
                for(int i=0;i<oldStudents.length();i++){
                    JSONObject os=oldStudents.optJSONObject(i); if(os==null)continue;
                    JSONObject ns=null;
                    for(int j=0;j<newStudents.length();j++)if(os.optString("username").equalsIgnoreCase(newStudents.optJSONObject(j).optString("username"))){ns=newStudents.optJSONObject(j);break;}
                    if(ns==null){newStudents.put(os);continue;}
                    for(String key:new String[]{"resources","messages","bookings"}){
                        JSONArray oa=os.optJSONArray(key), na=ns.optJSONArray(key);
                        if(oa!=null&&na!=null)for(int x=0;x<oa.length();x++)na.put(oa.opt(x));
                    }
                }
            }
            JSONArray oldAn=old.optJSONArray("announcements"), newAn=fresh.optJSONArray("announcements");
            if(oldAn!=null&&newAn!=null)for(int i=0;i<oldAn.length();i++){JSONObject a=oldAn.optJSONObject(i);if(a!=null&&!containsAnnouncement(newAn,a))newAn.put(a);}
            JSONArray oldPay=old.optJSONArray("payments"), newPay=fresh.optJSONArray("payments");
            if(oldPay!=null&&newPay!=null)for(int i=0;i<oldPay.length();i++){JSONObject p=oldPay.optJSONObject(i);if(p!=null&&!containsPayment(newPay,p))newPay.put(p);}
            JSONObject oldSettings=old.optJSONObject("settings");
            if(oldSettings!=null)fresh.put("settings",new JSONObject(oldSettings.toString()));
        }catch(Exception ignored){}
    }

    private boolean containsAnnouncement(JSONArray a,JSONObject x){for(int i=0;i<a.length();i++){JSONObject y=a.optJSONObject(i);if(y!=null&&y.optString("title").equals(x.optString("title"))&&y.optString("date").equals(x.optString("date")))return true;}return false;}
    private boolean containsPayment(JSONArray a,JSONObject x){for(int i=0;i<a.length();i++){JSONObject y=a.optJSONObject(i);if(y!=null&&y.optString("payer").equals(x.optString("payer"))&&y.optString("date").equals(x.optString("date")))return true;}return false;}

    private void ensureShape() throws JSONException {
        if(!data.has("students")) data.put("students",new JSONArray());
        if(!data.has("announcements")) data.put("announcements",new JSONArray());
        if(!data.has("schedule")) data.put("schedule",new JSONArray());
        if(!data.has("payments")) data.put("payments",new JSONArray());
        if(!data.has("settings")) data.put("settings",new JSONObject());
    }

    private JSONObject student(String user,String pass,String name,String grade,String curriculum,String subjects,int target,String goal,JSONArray topics) throws JSONException {
        JSONObject s=new JSONObject();
        s.put("username",user); s.put("password",pass); s.put("name",name); s.put("grade",grade);
        s.put("curriculum",curriculum); s.put("subjects",subjects); s.put("planMonth","October 2026");
        s.put("lessonTarget",target); s.put("planGoal",goal); s.put("topics",topics);
        s.put("lessons",new JSONArray()); s.put("resources",new JSONArray()); s.put("messages",new JSONArray()); s.put("bookings",new JSONArray());
        return s;
    }

    private JSONArray arr(String... values){ JSONArray a=new JSONArray(); for(String v:values)a.put(v); return a; }

    private JSONObject obj(String... kv) throws JSONException {
        JSONObject o=new JSONObject(); for(int i=0;i+1<kv.length;i+=2)o.put(kv[i],kv[i+1]); return o;
    }

    private JSONObject schedule(String date,String start,String end,String title,String visibility) throws JSONException {
        return obj("date",date,"start",start,"end",end,"title",title,"visibility",visibility);
    }

    private void addLesson(JSONObject s,String date,String time,int duration,String subject,String topic,String status,String notes) throws JSONException {
        JSONObject l=new JSONObject();
        l.put("date",date); l.put("time",time); l.put("duration",duration); l.put("subject",subject); l.put("topic",topic); l.put("status",status); l.put("notes",notes);
        s.getJSONArray("lessons").put(l);
    }

    private void save(){ prefs.edit().putString("data",data.toString()).apply(); }

    // ---------- WELCOME + LOGIN ----------

    private void showWelcome() {
        role=""; username="";
        ScrollView sv=new ScrollView(this);
        LinearLayout root=col(); root.setPadding(dp(20),dp(26),dp(20),dp(30)); root.setBackgroundColor(BG); sv.addView(root); setContentView(sv);

        LinearLayout brand=row(); brand.setGravity(Gravity.CENTER_VERTICAL);
        TextView mark=text("V",24,Color.WHITE,true); mark.setGravity(Gravity.CENTER); mark.setBackground(circle(ORANGE)); brand.addView(mark,new LinearLayout.LayoutParams(dp(52),dp(52)));
        LinearLayout bt=col(); bt.setPadding(dp(12),0,0,0); bt.addView(text("Victoria Tuition Academy",19,NAVY,true)); bt.addView(text("Better Understanding. Better Results.",11,MUTED,false)); brand.addView(bt,new LinearLayout.LayoutParams(0,-2,1));
        root.addView(brand);

        LinearLayout hero=col(); hero.setPadding(dp(22),dp(24),dp(22),dp(24)); hero.setBackground(gradient(NAVY,Color.rgb(30,41,59),22));
        TextView pill=pill("PRIVATE LEARNING PORTAL",Color.rgb(51,65,85),Color.WHITE); hero.addView(pill);
        TextView h=text("Everything for tutoring,\nin one app.",31,Color.WHITE,true); h.setPadding(0,dp(16),0,dp(10)); hero.addView(h);
        hero.addView(text("Choose your role to continue to your personalised workspace.",14,Color.rgb(203,213,225),false));
        root.addView(hero,marginTopBottom(28,22));

        root.addView(text("Continue as",18,NAVY,true));
        TextView hint=text("Your dashboard changes depending on your role.",12,MUTED,false); hint.setPadding(0,dp(4),0,dp(14)); root.addView(hint);

        roleCard(root,"Admin","Run lessons, students, schedule, bookings and finances.","ADMIN",ORANGE);
        roleCard(root,"Student","Your plan, lessons, booking, resources and tutor messages.","STUDENT",BLUE);
        roleCard(root,"Parent","View the learner’s plan, lessons and announcements.","PARENT",GREEN);
    }

    private void roleCard(LinearLayout root,String title,String subtitle,String value,int accent){
        LinearLayout c=row(); c.setGravity(Gravity.CENTER_VERTICAL); c.setPadding(dp(16),dp(16),dp(14),dp(16)); c.setBackground(round(Color.WHITE,18,1,LINE));
        TextView dot=text("•",34,accent,true); dot.setGravity(Gravity.CENTER); c.addView(dot,new LinearLayout.LayoutParams(dp(34),dp(44)));
        LinearLayout txt=col(); txt.addView(text(title,17,NAVY,true)); txt.addView(text(subtitle,12,MUTED,false)); c.addView(txt,new LinearLayout.LayoutParams(0,-2,1));
        c.addView(text("›",28,NAVY,false));
        c.setOnClickListener(v->{role=value;showLogin();});
        root.addView(c,marginBottom(10));
    }

    private void showLogin(){
        ScrollView sv=new ScrollView(this); LinearLayout root=col(); root.setPadding(dp(20),dp(24),dp(20),dp(30)); root.setBackgroundColor(BG); sv.addView(root); setContentView(sv);
        TextView back=text("‹  Back",15,NAVY,true); back.setPadding(0,dp(6),0,dp(24)); back.setOnClickListener(v->showWelcome()); root.addView(back);

        root.addView(pill(roleLabel().toUpperCase()+" ACCESS",ORANGE_SOFT,ORANGE));
        TextView title=text(role.equals("ADMIN")?"Welcome back, Victoria":"Welcome back",32,NAVY,true); title.setPadding(0,dp(14),0,dp(6)); root.addView(title);
        TextView sub=text(role.equals("ADMIN")?"Sign in to manage your tutoring business.":"Sign in to open your personalised learning portal.",14,MUTED,false); sub.setPadding(0,0,0,dp(22)); root.addView(sub);

        LinearLayout card=col(); card.setPadding(dp(18),dp(18),dp(18),dp(18)); card.setBackground(round(Color.WHITE,20,1,LINE));
        EditText user=input("Username",false); EditText pass=input("Password",true); card.addView(user); card.addView(pass,marginTopBottom(12,0));
        TextView error=text("",12,Color.rgb(185,28,28),true); error.setPadding(0,dp(8),0,0); card.addView(error);
        Button login=button("Sign in securely",ORANGE,Color.WHITE); card.addView(login,marginTopBottom(14,0));
        root.addView(card);

        login.setOnClickListener(v->{
            String u=user.getText().toString().trim(); String p=pass.getText().toString();
            if(role.equals("ADMIN")){
                if(u.equals("victoria.admin")&&p.equals("VTA-Admin26!")){username=u;showDashboard();}
                else error.setText("Incorrect admin login.");
            } else {
                JSONObject s=findStudent(u);
                if(s!=null&&p.equals(s.optString("password"))){username=u;showDashboard();}
                else error.setText("Incorrect student login.");
            }
        });
    }

    // ---------- APP SHELL ----------

    private void showDashboard(){
        LinearLayout root=col(); root.setBackgroundColor(BG);
        LinearLayout header=row(); header.setGravity(Gravity.CENTER_VERTICAL); header.setPadding(dp(18),dp(14),dp(14),dp(14)); header.setBackgroundColor(NAVY);
        TextView mark=text("V",18,Color.WHITE,true); mark.setGravity(Gravity.CENTER); mark.setBackground(circle(ORANGE)); header.addView(mark,new LinearLayout.LayoutParams(dp(38),dp(38)));
        LinearLayout ht=col(); ht.setPadding(dp(10),0,0,0); ht.addView(text("Victoria Tuition Academy",15,Color.WHITE,true)); ht.addView(text(roleLabel()+" workspace",10,Color.rgb(203,213,225),false)); header.addView(ht,new LinearLayout.LayoutParams(0,-2,1));
        TextView out=pill("Log out",Color.rgb(51,65,85),Color.WHITE); out.setOnClickListener(v->showWelcome()); header.addView(out);
        root.addView(header);

        ScrollView sv=new ScrollView(this); body=col(); body.setPadding(dp(16),dp(16),dp(16),dp(100)); sv.addView(body); root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));

        HorizontalScrollView hsv=new HorizontalScrollView(this); hsv.setHorizontalScrollBarEnabled(false);
        LinearLayout nav=row(); nav.setPadding(dp(8),dp(8),dp(8),dp(8)); nav.setBackgroundColor(Color.WHITE);
        String[] tabs=role.equals("ADMIN")?new String[]{"Home","Students","Schedule","Finance","Messages","Resources","Format"}:new String[]{"Home","Plan","Lessons","Book","Resources","Messages"};
        for(String t:tabs){ TextView b=pill(t,Color.WHITE,NAVY); b.setGravity(Gravity.CENTER); b.setPadding(dp(16),dp(12),dp(16),dp(12)); b.setOnClickListener(v->openTab(t)); nav.addView(b,marginRight(6)); }
        hsv.addView(nav); root.addView(hsv);
        setContentView(root);
        openTab("Home");
    }

    private void openTab(String tab){
        body.removeAllViews();
        if(role.equals("ADMIN")){
            switch(tab){
                case "Students": adminStudents(); break;
                case "Schedule": adminSchedule(); break;
                case "Finance": adminFinance(); break;
                case "Messages": adminMessages(); break;
                case "Resources": adminResources(); break;
                case "Format": adminFormat(); break;
                default: adminHome();
            }
        } else {
            JSONObject s=findStudent(username); if(s==null){body.addView(text("Student record not found.",16,NAVY,true));return;}
            switch(tab){
                case "Plan": studentPlan(s); break;
                case "Lessons": studentLessons(s); break;
                case "Book": dialogBooking(s); openTab("Lessons"); break;
                case "Resources": studentResources(s); break;
                case "Messages": studentMessages(s); break;
                default: studentHome(s);
            }
        }
    }

    // ---------- ADMIN ----------

    private void adminHome(){
        body.addView(text(greeting()+", Victoria",13,MUTED,true));
        body.addView(text("Your tutoring week",30,NAVY,true));
        TextView sub=text("Lessons, students, requests and money — all in one place.",13,MUTED,false); sub.setPadding(0,dp(4),0,dp(16)); body.addView(sub);

        LinearLayout hero=row(); hero.setPadding(dp(18),dp(18),dp(18),dp(18)); hero.setBackground(gradient(NAVY,NAVY2,20));
        LinearLayout hleft=col(); hleft.addView(text("THIS WEEK",11,Color.rgb(148,163,184),true)); hleft.addView(text(countUpcoming()+" lessons",27,Color.WHITE,true)); hleft.addView(text("R"+invoiceTotal()+" lesson value logged",12,Color.rgb(203,213,225),false)); hero.addView(hleft,new LinearLayout.LayoutParams(0,-2,1));
        LinearLayout hr=col(); hr.setGravity(Gravity.RIGHT); hr.addView(pill(pendingBookings()+" pending",ORANGE,Color.WHITE)); TextView unread=text(unreadMessages()+" unread messages",11,Color.rgb(203,213,225),false); unread.setPadding(0,dp(8),0,0); hr.addView(unread); hero.addView(hr);
        body.addView(hero,marginBottom(14));

        LinearLayout stats=row();
        stats.addView(metric("Students",String.valueOf(data.optJSONArray("students").length()),BLUE),new LinearLayout.LayoutParams(0,-2,1));
        stats.addView(metric("Requests",String.valueOf(pendingBookings()),ORANGE),withWeightMargin(1,8));
        stats.addView(metric("Paid","15th",GREEN),withWeightMargin(1,8));
        body.addView(stats);

        section("This week");
        List<JSONObject> lessons=allLessonsSorted();
        int shown=0;
        for(JSONObject l:lessons){
            if(!"Completed".equals(l.optString("_status")) && shown<5){ body.addView(lessonCard(l),marginBottom(9)); shown++; }
        }
        if(shown==0) body.addView(emptyCard("No upcoming lessons recorded."));

        section("Needs your attention");
        if(pendingBookings()==0 && unreadMessages()==0) body.addView(emptyCard("You’re all caught up."));
        else {
            if(pendingBookings()>0) actionCard("Booking requests",pendingBookings()+" request(s) need a response",ORANGE,v->adminSchedule());
            if(unreadMessages()>0) actionCard("Student messages",unreadMessages()+" unread message(s)",BLUE,v->adminMessages());
        }

        section("Quick actions");
        LinearLayout q1=row(); q1.addView(quick("＋","Student","Create portal",ORANGE,v->dialogCreateStudent()),new LinearLayout.LayoutParams(0,-2,1));
        q1.addView(quick("▣","Lesson","Log / plan",BLUE,v->dialogAddLesson(null)),withWeightMargin(1,8)); body.addView(q1,marginBottom(8));
        LinearLayout q2=row(); q2.addView(quick("↑","Resource","Upload",PURPLE,v->chooseStudentForResource()),new LinearLayout.LayoutParams(0,-2,1));
        q2.addView(quick("●","Announcement","Post",GREEN,v->dialogAnnouncement()),withWeightMargin(1,8)); body.addView(q2);
    }

    private void adminStudents(){
        pageTitle("Students","Create portals, edit learning plans and open each learner’s record.");
        Button add=primary("＋ Create student portal"); add.setOnClickListener(v->dialogCreateStudent()); body.addView(add,marginBottom(14));
        JSONArray a=data.optJSONArray("students");
        for(int i=0;i<a.length();i++){
            JSONObject s=a.optJSONObject(i);
            LinearLayout c=card();
            LinearLayout top=row(); top.setGravity(Gravity.CENTER_VERTICAL);
            TextView avatar=text(initials(s.optString("name")),15,Color.WHITE,true); avatar.setGravity(Gravity.CENTER); avatar.setBackground(circle(colorFor(i))); top.addView(avatar,new LinearLayout.LayoutParams(dp(44),dp(44)));
            LinearLayout txt=col(); txt.setPadding(dp(12),0,0,0); txt.addView(text(s.optString("name"),18,NAVY,true)); txt.addView(text(s.optString("grade")+" · "+s.optString("curriculum"),11,MUTED,false)); top.addView(txt,new LinearLayout.LayoutParams(0,-2,1));
            top.addView(pill(s.optInt("lessonTarget")+" / month",GREEN_SOFT,Color.rgb(21,128,61)));
            c.addView(top);
            TextView subjects=text(s.optString("subjects"),13,TEXT,false); subjects.setPadding(0,dp(12),0,dp(12)); c.addView(subjects);
            LinearLayout buttons=row();
            Button plan=smallButton("Edit plan"); plan.setOnClickListener(v->dialogEditPlan(s)); buttons.addView(plan,new LinearLayout.LayoutParams(0,dp(44),1));
            Button lesson=smallButton("Add lesson"); lesson.setOnClickListener(v->dialogAddLesson(s)); buttons.addView(lesson,withWeightMarginHeight(1,8,44));
            Button resource=smallButton("Resource"); resource.setOnClickListener(v->dialogResourceMeta(s)); buttons.addView(resource,withWeightMarginHeight(1,8,44));
            c.addView(buttons);
            TextView login=text("Login: "+s.optString("username")+"  •  temporary password stored",11,MUTED,false); login.setPadding(0,dp(10),0,0); c.addView(login);
            body.addView(c,marginBottom(12));
        }
    }

    private void adminSchedule(){
        pageTitle("Schedule","Your lessons, shared schedule and student booking requests.");
        LinearLayout actions=row();
        Button add=primary("＋ Add lesson"); add.setOnClickListener(v->dialogAddLesson(null)); actions.addView(add,new LinearLayout.LayoutParams(0,dp(48),1));
        Button event=secondary("＋ Calendar item"); event.setOnClickListener(v->dialogScheduleItem()); actions.addView(event,withWeightMarginHeight(1,8,48));
        body.addView(actions,marginBottom(14));

        section("Booking requests");
        int req=0;
        JSONArray students=data.optJSONArray("students");
        for(int i=0;i<students.length();i++){
            JSONObject s=students.optJSONObject(i); JSONArray bs=s.optJSONArray("bookings");
            for(int j=bs.length()-1;j>=0;j--){
                JSONObject b=bs.optJSONObject(j); if("Pending".equals(b.optString("status"))){ req++; body.addView(requestCard(s,b),marginBottom(9)); }
            }
        }
        if(req==0) body.addView(emptyCard("No pending booking requests."));

        section("Calendar");
        JSONArray sc=data.optJSONArray("schedule");
        List<JSONObject> events=new ArrayList<>();
        for(int i=0;i<sc.length();i++)events.add(sc.optJSONObject(i));
        Collections.sort(events,(a,b)->(a.optString("date")+a.optString("start")).compareTo(b.optString("date")+b.optString("start")));
        for(JSONObject e:events){
            LinearLayout c=card(); LinearLayout top=row();
            LinearLayout d=col(); d.addView(text(dayShort(e.optString("date")),11,ORANGE,true)); d.addView(text(dayNum(e.optString("date")),24,NAVY,true)); top.addView(d,new LinearLayout.LayoutParams(dp(52),-2));
            LinearLayout info=col(); info.addView(text(e.optString("title"),15,NAVY,true)); info.addView(text(e.optString("start")+"–"+e.optString("end")+"  ·  "+e.optString("visibility"),12,MUTED,false)); top.addView(info,new LinearLayout.LayoutParams(0,-2,1));
            c.addView(top); body.addView(c,marginBottom(9));
        }
    }

    private void adminFinance(){
        pageTitle("Finance & invoices","Track taught lessons, invoice-ready hours and payments received.");
        LinearLayout hero=card(); hero.setBackground(gradient(NAVY,NAVY2,20)); hero.addView(text("INVOICE-READY TOTAL",11,Color.rgb(148,163,184),true)); hero.addView(text("R"+invoiceTotal(),32,Color.WHITE,true)); hero.addView(text(completedHours()+" completed hours × R"+RATE+"/hour",12,Color.rgb(203,213,225),false));
        Button copy=button("Copy invoice summary",ORANGE,Color.WHITE); copy.setOnClickListener(v->copyInvoiceSummary()); hero.addView(copy,marginTopBottom(14,0)); body.addView(hero,marginBottom(14));

        section("Completed lesson log");
        List<JSONObject> ls=allLessonsSorted(); int completed=0;
        for(JSONObject l:ls) if("Completed".equals(l.optString("_status"))){ completed++; LinearLayout c=card(); c.addView(text(l.optString("_student")+" · "+l.optString("topic"),15,NAVY,true)); c.addView(text(l.optString("date")+" · "+l.optInt("duration")+" min · "+l.optString("subject"),12,MUTED,false)); c.addView(text("R"+((l.optInt("duration")*RATE)/60),13,GREEN,true)); body.addView(c,marginBottom(8)); }
        if(completed==0) body.addView(emptyCard("No completed lessons logged yet."));

        section("Payments received");
        Button add=secondary("＋ Record payment"); add.setOnClickListener(v->dialogPayment()); body.addView(add,marginBottom(10));
        JSONArray ps=data.optJSONArray("payments");
        for(int i=ps.length()-1;i>=0;i--){
            JSONObject p=ps.optJSONObject(i); LinearLayout c=card();
            c.addView(text(p.optString("payer"),16,NAVY,true));
            c.addView(text(prettyDate(p.optString("date")),12,MUTED,false));
            c.addView(text(p.optInt("amount",-1)<0?"Amount not entered":"R"+p.optInt("amount"),17,p.optInt("amount",-1)<0?ORANGE:GREEN,true));
            if(!p.optString("note").isEmpty())c.addView(text(p.optString("note"),12,MUTED,false));
            body.addView(c,marginBottom(8));
        }
    }

    private void adminMessages(){
        pageTitle("Messages","Student conversations and academy announcements.");
        Button announce=primary("＋ Post announcement"); announce.setOnClickListener(v->dialogAnnouncement()); body.addView(announce,marginBottom(14));

        section("Inbox");
        JSONArray students=data.optJSONArray("students"); int total=0;
        for(int i=0;i<students.length();i++){
            JSONObject s=students.optJSONObject(i); JSONArray msgs=s.optJSONArray("messages");
            for(int j=msgs.length()-1;j>=0;j--){
                JSONObject m=msgs.optJSONObject(j); total++;
                LinearLayout c=card(); LinearLayout top=row(); top.addView(text(s.optString("name"),15,NAVY,true),new LinearLayout.LayoutParams(0,-2,1)); top.addView(pill(m.optString("from"),Color.rgb(241,245,249),MUTED)); c.addView(top);
                c.addView(text(m.optString("text"),13,TEXT,false),marginTopBottom(8,0));
                c.addView(text(m.optString("date"),11,MUTED,false));
                Button reply=smallButton("Reply"); reply.setOnClickListener(v->dialogAdminReply(s)); c.addView(reply,marginTopBottom(10,0));
                m.remove("unread"); try{m.put("unread",false);}catch(Exception ignored){}
                body.addView(c,marginBottom(9));
            }
        }
        save();
        if(total==0) body.addView(emptyCard("No student messages yet."));

        section("Announcements");
        JSONArray an=data.optJSONArray("announcements");
        for(int i=an.length()-1;i>=0;i--){ JSONObject a=an.optJSONObject(i); LinearLayout c=card(); c.addView(text(a.optString("title"),15,NAVY,true)); c.addView(text(a.optString("message"),13,MUTED,false)); c.addView(text(a.optString("date"),11,MUTED,false)); body.addView(c,marginBottom(8)); }
    }

    private void adminResources(){
        pageTitle("Resource library","Upload slides, notes, worksheets and files from your phone, then preview exactly what was saved.");
        Button add=primary("↑ Upload from phone"); add.setOnClickListener(v->chooseStudentForResource()); body.addView(add,marginBottom(14));
        JSONArray students=data.optJSONArray("students"); int total=0;
        for(int i=0;i<students.length();i++){
            JSONObject s=students.optJSONObject(i); JSONArray rs=s.optJSONArray("resources");
            for(int j=rs.length()-1;j>=0;j--){
                total++; JSONObject r=rs.optJSONObject(j); LinearLayout card=card();
                LinearLayout top=row(); top.addView(pill(r.optString("type"),Color.rgb(245,243,255),PURPLE)); top.addView(text(s.optString("name"),12,MUTED,true),withWeightMargin(1,10)); card.addView(top);
                card.addView(text(r.optString("title"),16,NAVY,true),marginTopBottom(10,2));
                if(!r.optString("fileName").isEmpty())card.addView(text(r.optString("fileName"),11,MUTED,false));
                if(!r.optString("description").isEmpty())card.addView(text(r.optString("description"),12,TEXT,false),marginTopBottom(6,0));
                LinearLayout actions=row();
                Button preview=smallButton("Preview"); preview.setOnClickListener(v->previewResource(r)); actions.addView(preview,new LinearLayout.LayoutParams(0,dp(44),1));
                Button open=smallButton("Open file"); open.setOnClickListener(v->openResource(r.optString("uri"))); actions.addView(open,withWeightMarginHeight(1,8,44));
                card.addView(actions,marginTopBottom(12,0)); body.addView(card,marginBottom(9));
            }
        }
        if(total==0) body.addView(emptyCard("No resources uploaded yet."));
    }

    // ---------- STUDENT ----------

    // ---------- STUDENT ----------

    private void studentHome(JSONObject s){
        body.addView(pill(s.optString("grade")+" · "+s.optString("curriculum"),Color.rgb(239,246,255),BLUE));
        TextView hi=text("Hi, "+s.optString("name")+" 👋",30,NAVY,true); hi.setPadding(0,dp(12),0,dp(3)); body.addView(hi);
        body.addView(text("Here’s what matters for your learning right now.",13,MUTED,false));

        LinearLayout plan=card(); plan.setBackground(gradient(NAVY,NAVY2,20)); plan.addView(text(s.optString("planMonth").toUpperCase()+" PLAN",10,Color.rgb(148,163,184),true));
        plan.addView(text(s.optInt("lessonTarget")+" lessons planned",26,Color.WHITE,true)); plan.addView(text(s.optString("planGoal"),12,Color.rgb(203,213,225),false));
        body.addView(plan,marginTopBottom(16,12));

        section("Next lesson");
        JSONObject next=nextLesson(s);
        if(next==null) body.addView(emptyCard("No upcoming lesson yet. Use Book to request one."));
        else {
            LinearLayout c=card(); c.addView(pill(next.optString("status"),GREEN_SOFT,Color.rgb(21,128,61)));
            c.addView(text(next.optString("topic"),18,NAVY,true),marginTopBottom(10,2));
            c.addView(text(prettyDate(next.optString("date"))+" · "+next.optString("time")+" · "+next.optInt("duration")+" min",12,MUTED,false)); c.addView(text(next.optString("subject"),12,ORANGE,true)); body.addView(c);
        }

        section("Focus topics");
        JSONArray topics=s.optJSONArray("topics"); LinearLayout wraps=col();
        for(int i=0;i<Math.min(4,topics.length());i++){TextView p=pill("✓  "+topics.optString(i),Color.WHITE,NAVY); p.setBackground(round(Color.WHITE,13,1,LINE)); wraps.addView(p,marginBottom(7));}
        body.addView(wraps);

        section("Announcements");
        JSONArray an=data.optJSONArray("announcements");
        for(int i=an.length()-1;i>=0;i--){JSONObject a=an.optJSONObject(i);LinearLayout c=card();c.addView(text(a.optString("title"),15,NAVY,true));c.addView(text(a.optString("message"),12,MUTED,false));body.addView(c,marginBottom(8));}

        section("Tutor availability");
        addAvailabilityPreview();
    }

    private void studentPlan(JSONObject s){
        pageTitle("My learning plan",s.optString("planMonth")+" · built around your exam goals.");
        LinearLayout progress=card(); progress.addView(text("MONTHLY TARGET",10,MUTED,true)); progress.addView(text(s.optInt("lessonTarget")+" lessons",29,ORANGE,true)); progress.addView(text(s.optString("planGoal"),13,TEXT,false)); body.addView(progress,marginBottom(12));
        section("Focus topics");
        JSONArray t=s.optJSONArray("topics"); for(int i=0;i<t.length();i++){LinearLayout c=card();c.addView(text(String.format(Locale.getDefault(),"%02d",i+1),11,ORANGE,true));c.addView(text(t.optString(i),15,NAVY,true));body.addView(c,marginBottom(8));}
    }

    private void studentLessons(JSONObject s){
        pageTitle("My lessons","See what you’ve covered and what’s coming next.");
        Button book=primary("＋ Request another lesson"); book.setOnClickListener(v->dialogBooking(s)); body.addView(book,marginBottom(14));

        JSONArray bs=s.optJSONArray("bookings"); if(bs.length()>0){section("Booking requests");for(int i=bs.length()-1;i>=0;i--){JSONObject b=bs.optJSONObject(i);LinearLayout c=card();c.addView(pill(b.optString("status"),ORANGE_SOFT,ORANGE));c.addView(text(prettyDate(b.optString("date"))+" · "+b.optString("time"),15,NAVY,true),marginTopBottom(8,2));c.addView(text(b.optString("subject")+" · "+b.optString("topic"),12,MUTED,false));body.addView(c,marginBottom(8));}}

        section("Lesson history");
        JSONArray ls=s.optJSONArray("lessons");
        for(int i=ls.length()-1;i>=0;i--){JSONObject l=ls.optJSONObject(i);LinearLayout c=card();LinearLayout top=row();top.addView(text(l.optString("topic"),15,NAVY,true),new LinearLayout.LayoutParams(0,-2,1));top.addView(pill(l.optString("status"),statusBg(l.optString("status")),statusColor(l.optString("status"))));c.addView(top);c.addView(text(prettyDate(l.optString("date"))+" · "+l.optString("time")+" · "+l.optInt("duration")+" min",12,MUTED,false));c.addView(text(l.optString("subject"),12,ORANGE,true));if(!l.optString("notes").isEmpty())c.addView(text(l.optString("notes"),12,TEXT,false),marginTopBottom(8,0));body.addView(c,marginBottom(8));}
    }

    private void studentResources(JSONObject s){
        pageTitle("My resources","Everything Victoria has shared for your lessons and exams.");
        JSONArray rs=s.optJSONArray("resources");
        if(rs.length()==0)body.addView(emptyCard("No resources have been added yet."));
        for(int i=rs.length()-1;i>=0;i--){
            JSONObject r=rs.optJSONObject(i); LinearLayout card=card();
            card.addView(pill(r.optString("type"),Color.rgb(245,243,255),PURPLE));
            card.addView(text(r.optString("title"),16,NAVY,true),marginTopBottom(9,3));
            if(!r.optString("fileName").isEmpty())card.addView(text(r.optString("fileName"),11,MUTED,false));
            card.addView(text(r.optString("description"),12,MUTED,false));
            LinearLayout actions=row();
            Button preview=smallButton("Preview"); preview.setOnClickListener(v->previewResource(r)); actions.addView(preview,new LinearLayout.LayoutParams(0,dp(44),1));
            Button open=smallButton("Open"); open.setOnClickListener(v->openResource(r.optString("uri"))); actions.addView(open,withWeightMarginHeight(1,8,44));
            card.addView(actions,marginTopBottom(10,0)); body.addView(card,marginBottom(9));
        }
    }

    private void studentMessages(JSONObject s){
        pageTitle("Tutor messages","Ask questions between lessons and keep everything in one thread.");
        Button send=primary("＋ Message Victoria"); send.setOnClickListener(v->dialogStudentMessage(s)); body.addView(send,marginBottom(14));
        JSONArray ms=s.optJSONArray("messages");
        if(ms.length()==0)body.addView(emptyCard("No messages yet."));
        for(int i=ms.length()-1;i>=0;i--){JSONObject m=ms.optJSONObject(i);LinearLayout c=card();c.addView(text(m.optString("from"),13,m.optString("from").equals("Victoria")?ORANGE:BLUE,true));c.addView(text(m.optString("text"),13,TEXT,false),marginTopBottom(6,3));c.addView(text(m.optString("date"),10,MUTED,false));body.addView(c,marginBottom(8));}
    }

    // ---------- DIALOGS + ACTIONS ----------

    private void dialogCreateStudent(){
        LinearLayout box=formBox();
        EditText name=input("Student name",false), grade=input("Grade",false), curriculum=input("Curriculum",false), subjects=input("Subjects",false), user=input("Username",false), pass=input("Temporary password",false);
        for(EditText e:new EditText[]{name,grade,curriculum,subjects,user,pass})box.addView(e,marginBottom(8));
        new AlertDialog.Builder(this).setTitle("Create student portal").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Create",(d,w)->{
            try{
                JSONObject s=student(user.getText().toString().trim(),pass.getText().toString(),name.getText().toString(),grade.getText().toString(),curriculum.getText().toString(),subjects.getText().toString(),4,"Build confidence and make measurable progress this month.",new JSONArray());
                data.optJSONArray("students").put(s); save(); adminStudents();
            }catch(Exception ignored){}
        }).show();
    }

    private void dialogEditPlan(JSONObject s){
        LinearLayout box=formBox(); EditText month=input("Month",false), target=input("Lessons this month",false), goal=input("Goal",false), topics=input("Focus topics, separated by commas",false);
        month.setText(s.optString("planMonth")); target.setText(String.valueOf(s.optInt("lessonTarget"))); goal.setText(s.optString("planGoal"));
        List<String> ts=new ArrayList<>();JSONArray a=s.optJSONArray("topics");for(int i=0;i<a.length();i++)ts.add(a.optString(i));topics.setText(android.text.TextUtils.join(", ",ts));
        for(EditText e:new EditText[]{month,target,goal,topics})box.addView(e,marginBottom(8));
        new AlertDialog.Builder(this).setTitle("Edit "+s.optString("name")+" plan").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{
            try{s.put("planMonth",month.getText().toString());s.put("lessonTarget",Integer.parseInt(target.getText().toString().trim().isEmpty()?"0":target.getText().toString().trim()));s.put("planGoal",goal.getText().toString());JSONArray ta=new JSONArray();for(String x:topics.getText().toString().split(","))if(!x.trim().isEmpty())ta.put(x.trim());s.put("topics",ta);save();adminStudents();}catch(Exception ignored){}
        }).show();
    }

    private void dialogAddLesson(JSONObject fixed){
        LinearLayout box=formBox(); Spinner studentSpin=studentSpinner();
        final String[] date={""}, time={""};
        Button dateBtn=pickerButton("Choose lesson date"); Button timeBtn=pickerButton("Choose start time");
        dateBtn.setOnClickListener(v->pickDate(dateBtn,date,()->{}));
        timeBtn.setOnClickListener(v->pickTime(timeBtn,time));
        Spinner duration=new Spinner(this); duration.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"60 minutes","120 minutes"}));
        EditText subject=input("Subject",false), topic=input("Topic taught / to teach",false), status=input("Status: Planned / Confirmed / Completed",false), notes=input("Lesson notes",false); status.setText("Planned");
        if(fixed==null)box.addView(studentSpin,marginBottom(8));
        box.addView(dateBtn,marginBottom(8)); box.addView(timeBtn,marginBottom(8)); box.addView(duration,marginBottom(8));
        for(EditText e:new EditText[]{subject,topic,status,notes})box.addView(e,marginBottom(8));
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle(fixed==null?"Add lesson":"Add lesson for "+fixed.optString("name")).setView(box).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();
        dialog.setOnShowListener(x->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            if(date[0].isEmpty()||time[0].isEmpty()){Toast.makeText(this,"Choose a date and time first.",Toast.LENGTH_SHORT).show();return;}
            JSONObject s=fixed!=null?fixed:studentAt(studentSpin.getSelectedItemPosition()); if(s==null)return;
            try{
                int mins=duration.getSelectedItemPosition()==1?120:60;
                addLesson(s,date[0],time[0],mins,subject.getText().toString(),topic.getText().toString(),status.getText().toString(),notes.getText().toString());
                if(!"Completed".equalsIgnoreCase(status.getText().toString()))syncLessonToSchedule(s,date[0],time[0],mins,topic.getText().toString());
                save(); dialog.dismiss(); adminSchedule();
            }catch(Exception ignored){}
        }));
        dialog.show();
    }

    private void syncLessonToSchedule(JSONObject s,String date,String time,int mins,String topic){
        try{
            String end=endTime(time,mins); data.optJSONArray("schedule").put(schedule(date,time,end,s.optString("name")+" — "+topic,"Students"));
        }catch(Exception ignored){}
    }

    private void dialogScheduleItem(){
        LinearLayout box=formBox(); final String[] date={""},start={""},end={""};
        Button dateBtn=pickerButton("Choose date"), startBtn=pickerButton("Choose start time"), endBtn=pickerButton("Choose end time");
        dateBtn.setOnClickListener(v->pickDate(dateBtn,date,()->{})); startBtn.setOnClickListener(v->pickTime(startBtn,start)); endBtn.setOnClickListener(v->pickTime(endBtn,end));
        EditText title=input("Schedule item",false); Spinner vis=new Spinner(this); vis.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Private","Students"}));
        box.addView(dateBtn,marginBottom(8));box.addView(startBtn,marginBottom(8));box.addView(endBtn,marginBottom(8));box.addView(title,marginBottom(8));box.addView(vis);
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Add calendar item").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Add",null).create();
        dialog.setOnShowListener(x->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            if(date[0].isEmpty()||start[0].isEmpty()||end[0].isEmpty()){Toast.makeText(this,"Choose the date and times.",Toast.LENGTH_SHORT).show();return;}
            try{data.optJSONArray("schedule").put(schedule(date[0],start[0],end[0],title.getText().toString(),vis.getSelectedItem().toString()));save();dialog.dismiss();adminSchedule();}catch(Exception ignored){}
        })); dialog.show();
    }

    private void dialogBooking(JSONObject s){
        LinearLayout box=formBox();
        TextView learner=text(s.optString("name")+" · "+s.optString("grade"),13,NAVY,true); learner.setPadding(dp(12),dp(10),dp(12),dp(10)); learner.setBackground(round(Color.rgb(248,250,252),12,1,LINE)); box.addView(learner,marginBottom(10));

        final String[] chosenDate={""}, chosenTime={""};
        Button dateBtn=pickerButton("1. Choose date");
        box.addView(dateBtn,marginBottom(10));

        TextView durationLabel=text("2. Lesson duration",12,MUTED,true); box.addView(durationLabel);
        Spinner duration=new Spinner(this); duration.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"1 hour","2 hours"})); box.addView(duration,marginBottom(10));

        TextView timeLabel=text("3. Choose an available time",12,MUTED,true); box.addView(timeLabel);
        GridLayout slots=new GridLayout(this); slots.setColumnCount(3); box.addView(slots,marginBottom(12));

        Spinner subject=new Spinner(this); subject.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Mathematics","Physics","Natural Sciences"})); box.addView(subject,marginBottom(8));
        EditText topic=input("Topic",false), help=input("What exactly do you need help with?",false); box.addView(topic,marginBottom(8));box.addView(help);

        Runnable refresh=()->renderTimeSlots(slots,chosenDate[0],duration.getSelectedItemPosition()==1?120:60,chosenTime);
        dateBtn.setOnClickListener(v->pickDate(dateBtn,chosenDate,refresh));
        duration.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener(){
            public void onItemSelected(android.widget.AdapterView<?> p,View v,int pos,long id){chosenTime[0]="";refresh.run();}
            public void onNothingSelected(android.widget.AdapterView<?> p){}
        });
        renderTimeSlots(slots,"",60,chosenTime);

        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Request a lesson").setMessage("You will only see free time slots. Other students’ names and schedule details stay private.").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Send request",null).create();
        dialog.setOnShowListener(x->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            if(chosenDate[0].isEmpty()){Toast.makeText(this,"Choose a date.",Toast.LENGTH_SHORT).show();return;}
            if(chosenTime[0].isEmpty()){Toast.makeText(this,"Choose an available time.",Toast.LENGTH_SHORT).show();return;}
            try{
                JSONObject b=new JSONObject();b.put("date",chosenDate[0]);b.put("time",chosenTime[0]);b.put("duration",duration.getSelectedItem().toString());b.put("subject",subject.getSelectedItem().toString());b.put("topic",topic.getText().toString());b.put("help",help.getText().toString());b.put("status","Pending");
                s.optJSONArray("bookings").put(b);save();notifyAdmin("New booking request",s.optString("name")+" requested "+chosenDate[0]+" at "+chosenTime[0]);dialog.dismiss();studentLessons(s);
            }catch(Exception ignored){}
        })); dialog.show();
    }

    private LinearLayout requestCard(JSONObject s,JSONObject b){
        LinearLayout c=card(); LinearLayout top=row(); top.addView(text(s.optString("name"),16,NAVY,true),new LinearLayout.LayoutParams(0,-2,1)); top.addView(pill("PENDING",ORANGE_SOFT,ORANGE)); c.addView(top);
        c.addView(text(prettyDate(b.optString("date"))+" · "+b.optString("time")+" · "+b.optString("duration"),13,TEXT,true),marginTopBottom(8,3)); c.addView(text(b.optString("subject")+" · "+b.optString("topic"),12,MUTED,false)); if(!b.optString("help").isEmpty())c.addView(text(b.optString("help"),12,TEXT,false),marginTopBottom(6,0));
        LinearLayout actions=row(); Button yes=smallButton("Confirm"); yes.setOnClickListener(v->{try{b.put("status","Confirmed");int mins=b.optString("duration").startsWith("2")?120:60;addLesson(s,b.optString("date"),b.optString("time"),mins,b.optString("subject"),b.optString("topic"),"Confirmed","Booked by student request.");syncLessonToSchedule(s,b.optString("date"),b.optString("time"),mins,b.optString("topic"));save();adminSchedule();}catch(Exception ignored){}});
        Button no=smallButton("Decline"); no.setOnClickListener(v->{try{b.put("status","Declined");save();adminSchedule();}catch(Exception ignored){}});
        actions.addView(yes,new LinearLayout.LayoutParams(0,dp(44),1)); actions.addView(no,withWeightMarginHeight(1,8,44)); c.addView(actions,marginTopBottom(12,0)); return c;
    }

    private void dialogStudentMessage(JSONObject s){
        EditText msg=input("Ask Victoria a question",false);
        new AlertDialog.Builder(this).setTitle("Message Victoria").setView(msg).setNegativeButton("Cancel",null).setPositiveButton("Send",(d,w)->{try{JSONObject m=obj("from",s.optString("name"),"text",msg.getText().toString(),"date",today());m.put("unread",true);s.optJSONArray("messages").put(m);save();notifyAdmin("New student message",s.optString("name")+": "+msg.getText());studentMessages(s);}catch(Exception ignored){}}).show();
    }

    private void dialogAdminReply(JSONObject s){
        EditText msg=input("Reply to "+s.optString("name"),false);
        new AlertDialog.Builder(this).setTitle("Reply").setView(msg).setNegativeButton("Cancel",null).setPositiveButton("Send",(d,w)->{try{JSONObject m=obj("from","Victoria","text",msg.getText().toString(),"date",today());m.put("unread",false);s.optJSONArray("messages").put(m);save();adminMessages();}catch(Exception ignored){}}).show();
    }

    private void dialogAnnouncement(){
        LinearLayout box=formBox(); EditText t=input("Announcement title",false),m=input("Message",false);box.addView(t,marginBottom(8));box.addView(m);
        new AlertDialog.Builder(this).setTitle("Post announcement").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Post",(d,w)->{try{data.optJSONArray("announcements").put(obj("title",t.getText().toString(),"message",m.getText().toString(),"date",today()));save();adminMessages();}catch(Exception ignored){}}).show();
    }

    private void dialogPayment(){
        LinearLayout box=formBox();EditText payer=input("Payer",false),date=input("Date (YYYY-MM-DD)",false),amount=input("Amount (R)",false),note=input("Note",false);amount.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);
        for(EditText e:new EditText[]{payer,date,amount,note})box.addView(e,marginBottom(8));
        new AlertDialog.Builder(this).setTitle("Record payment").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{try{JSONObject p=new JSONObject();p.put("payer",payer.getText().toString());p.put("date",date.getText().toString());p.put("amount",amount.getText().toString().trim().isEmpty()?-1:(int)Math.round(Double.parseDouble(amount.getText().toString())));p.put("note",note.getText().toString());data.optJSONArray("payments").put(p);save();adminFinance();}catch(Exception ignored){}}).show();
    }

    private void chooseStudentForResource(){
        JSONArray students=data.optJSONArray("students"); String[] names=new String[students.length()]; for(int i=0;i<students.length();i++)names[i]=students.optJSONObject(i).optString("name");
        new AlertDialog.Builder(this).setTitle("Choose student").setItems(names,(d,which)->dialogResourceMeta(students.optJSONObject(which))).show();
    }

    private void dialogResourceMeta(JSONObject s){
        LinearLayout box=formBox();EditText title=input("Resource title",false); Spinner type=new Spinner(this);type.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Slides","PDF / Notes","Worksheet","Video","Interactive Lesson","Image","Other"}));box.addView(title,marginBottom(8));box.addView(type);
        new AlertDialog.Builder(this).setTitle("Upload for "+s.optString("name")).setMessage("Choose the format you want, then select the file from your phone. You’ll get a preview after it saves.").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Choose file",(d,w)->{
            pendingResourceStudent=s;pendingResourceTitle=title.getText().toString();pendingResourceType=type.getSelectedItem().toString();Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");startActivityForResult(i,FILE_PICK);
        }).show();
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent intent){
        super.onActivityResult(requestCode,resultCode,intent);
        if(requestCode==FILE_PICK && resultCode==RESULT_OK && intent!=null && intent.getData()!=null && pendingResourceStudent!=null){
            Uri uri=intent.getData();
            try{getContentResolver().takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION);}catch(Exception ignored){}
            try{
                String fileName=getFileName(uri);
                JSONObject r=new JSONObject();r.put("title",pendingResourceTitle.isEmpty()?fileName:pendingResourceTitle);r.put("type",pendingResourceType);r.put("uri",uri.toString());r.put("fileName",fileName);r.put("description","Uploaded from Victoria’s phone.");
                pendingResourceStudent.optJSONArray("resources").put(r);save();
                JSONObject saved=r; String studentName=pendingResourceStudent.optString("name");
                pendingResourceStudent=null;
                adminResources();
                new AlertDialog.Builder(this).setTitle("Upload saved").setMessage(saved.optString("title")+"\n"+saved.optString("fileName")+"\nAdded to "+studentName+".").setNegativeButton("Close",null).setPositiveButton("Preview",(d,w)->previewResource(saved)).show();
            }catch(Exception e){Toast.makeText(this,"Could not save this upload.",Toast.LENGTH_SHORT).show();pendingResourceStudent=null;}
        }
    }

    private void openResource(String uri){
        try{Intent i=new Intent(Intent.ACTION_VIEW,Uri.parse(uri));i.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);startActivity(i);}catch(Exception e){Toast.makeText(this,"This resource is not available on this device.",Toast.LENGTH_SHORT).show();}
    }

    private void adminFormat(){
        pageTitle("App format","Choose how lesson and resource information is displayed in your app.");
        JSONObject settings=data.optJSONObject("settings");
        LinearLayout lesson=card(); lesson.addView(text("Lesson cards",15,NAVY,true)); lesson.addView(text(settings.optString("lessonFormat","Detailed cards"),12,MUTED,false)); Button lc=smallButton("Change format"); lc.setOnClickListener(v->chooseSetting("lessonFormat",new String[]{"Detailed cards","Compact cards"},"Lesson card format")); lesson.addView(lc,marginTopBottom(10,0)); body.addView(lesson,marginBottom(10));
        LinearLayout resource=card(); resource.addView(text("Resource display",15,NAVY,true)); resource.addView(text(settings.optString("resourceFormat","Preview cards"),12,MUTED,false)); Button rc=smallButton("Change format"); rc.setOnClickListener(v->chooseSetting("resourceFormat",new String[]{"Preview cards","Compact list"},"Resource format")); resource.addView(rc,marginTopBottom(10,0)); body.addView(resource,marginBottom(10));
        LinearLayout slot=card(); slot.addView(text("Booking time spacing",15,NAVY,true)); slot.addView(text(settings.optInt("slotInterval",30)+" minute slots",12,MUTED,false)); Button sc=smallButton("Change spacing"); sc.setOnClickListener(v->chooseSlotInterval()); slot.addView(sc,marginTopBottom(10,0)); body.addView(slot);
        TextView note=text("These settings change the presentation inside the app. Student privacy rules stay the same: they never see another learner’s schedule details.",12,MUTED,false); note.setPadding(0,dp(14),0,0); body.addView(note);
    }

    private void chooseSetting(String key,String[] options,String title){
        new AlertDialog.Builder(this).setTitle(title).setItems(options,(d,which)->{try{data.optJSONObject("settings").put(key,options[which]);save();adminFormat();}catch(Exception ignored){}}).show();
    }
    private void chooseSlotInterval(){
        String[] options={"30 minutes","60 minutes"};
        new AlertDialog.Builder(this).setTitle("Booking slot spacing").setItems(options,(d,which)->{try{data.optJSONObject("settings").put("slotInterval",which==0?30:60);save();adminFormat();}catch(Exception ignored){}}).show();
    }

    private void addAvailabilityPreview(){
        Calendar day=Calendar.getInstance();
        for(int i=0;i<5;i++){
            String date=new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH).format(day.getTime());
            int free=countAvailableSlots(date,60);
            LinearLayout card=card(); LinearLayout row=row(); row.addView(text(prettyDate(date),13,NAVY,true),new LinearLayout.LayoutParams(0,-2,1)); row.addView(pill(free+" free",free>0?GREEN_SOFT:Color.rgb(254,242,242),free>0?Color.rgb(21,128,61):Color.rgb(185,28,28))); card.addView(row); card.addView(text("09:00–19:00 · tap Book to choose a specific time",11,MUTED,false),marginTopBottom(5,0)); body.addView(card,marginBottom(7)); day.add(Calendar.DATE,1);
        }
    }

    private int countAvailableSlots(String date,int duration){
        int interval=data.optJSONObject("settings").optInt("slotInterval",30), n=0;
        for(int m=9*60;m+duration<=19*60;m+=interval)if(isTimeAvailable(date,m,duration))n++;
        return n;
    }

    private void renderTimeSlots(GridLayout grid,String date,int duration,String[] selected){
        grid.removeAllViews();
        if(date==null||date.isEmpty()){TextView h=text("Choose a date first.",12,MUTED,false);grid.addView(h);return;}
        int interval=data.optJSONObject("settings").optInt("slotInterval",30);
        for(int m=9*60;m+duration<=19*60;m+=interval){
            final int mins=m; final String label=minutesToTime(m); boolean free=isTimeAvailable(date,m,duration);
            Button b=new Button(this); b.setAllCaps(false); b.setText(label+"\n"+(free?"Available":"Unavailable")); b.setTextSize(10); b.setPadding(dp(4),dp(6),dp(4),dp(6)); b.setEnabled(free);
            b.setTextColor(free?NAVY:Color.rgb(148,163,184)); b.setBackground(round(free?Color.WHITE:Color.rgb(241,245,249),12,1,free?LINE:Color.rgb(226,232,240)));
            GridLayout.LayoutParams lp=new GridLayout.LayoutParams(); lp.width=0; lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f); lp.setMargins(dp(3),dp(3),dp(3),dp(3)); b.setLayoutParams(lp);
            if(free)b.setOnClickListener(v->{selected[0]=label;for(int i=0;i<grid.getChildCount();i++){View child=grid.getChildAt(i);if(child instanceof Button){Button bb=(Button)child;if(bb.isEnabled())bb.setBackground(round(Color.WHITE,12,1,LINE));}}b.setBackground(round(ORANGE_SOFT,12,2,ORANGE));});
            grid.addView(b);
        }
    }

    private boolean isTimeAvailable(String date,int start,int duration){
        int end=start+duration; JSONArray sc=data.optJSONArray("schedule");
        for(int i=0;i<sc.length();i++){JSONObject e=sc.optJSONObject(i);if(!date.equals(e.optString("date")))continue;int a=timeToMinutes(e.optString("start")),b=timeToMinutes(e.optString("end"));if(a>=0&&b>=0&&start<b&&end>a)return false;}
        return true;
    }

    private int timeToMinutes(String t){try{String[] p=t.split(":");return Integer.parseInt(p[0])*60+Integer.parseInt(p[1]);}catch(Exception e){return -1;}}
    private String minutesToTime(int m){return String.format(Locale.ENGLISH,"%02d:%02d",m/60,m%60);}

    private Button pickerButton(String label){Button b=secondary(label);b.setGravity(Gravity.LEFT|Gravity.CENTER_VERTICAL);return b;}
    private void pickDate(Button button,String[] holder,Runnable after){
        Calendar now=Calendar.getInstance();
        new DatePickerDialog(this,(v,y,m,d)->{holder[0]=String.format(Locale.ENGLISH,"%04d-%02d-%02d",y,m+1,d);button.setText(prettyDate(holder[0]));after.run();},now.get(Calendar.YEAR),now.get(Calendar.MONTH),now.get(Calendar.DAY_OF_MONTH)).show();
    }
    private void pickTime(Button button,String[] holder){
        Calendar now=Calendar.getInstance();
        new TimePickerDialog(this,(v,h,m)->{holder[0]=String.format(Locale.ENGLISH,"%02d:%02d",h,m);button.setText(holder[0]);},now.get(Calendar.HOUR_OF_DAY),0,true).show();
    }

    private String getFileName(Uri uri){
        String name="Uploaded file"; Cursor cursor=null;
        try{cursor=getContentResolver().query(uri,null,null,null,null);if(cursor!=null&&cursor.moveToFirst()){int idx=cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(idx>=0)name=cursor.getString(idx);}}catch(Exception ignored){}finally{if(cursor!=null)cursor.close();}
        return name;
    }

    private void previewResource(JSONObject r){
        LinearLayout box=formBox(); box.addView(pill(r.optString("type"),Color.rgb(245,243,255),PURPLE)); box.addView(text(r.optString("title"),18,NAVY,true),marginTopBottom(10,3));
        if(!r.optString("fileName").isEmpty())box.addView(text(r.optString("fileName"),12,MUTED,false));
        box.addView(text(r.optString("description"),12,TEXT,false),marginTopBottom(8,0));
        new AlertDialog.Builder(this).setTitle("Resource preview").setView(box).setNegativeButton("Close",null).setPositiveButton("Open file",(d,w)->openResource(r.optString("uri"))).show();
    }

    // ---------- NOTIFICATIONS ----------

    private void createNotificationChannel(){
        if(Build.VERSION.SDK_INT>=26){NotificationChannel c=new NotificationChannel("vta_admin","VTA Admin Alerts",NotificationManager.IMPORTANCE_DEFAULT);c.setDescription("Booking and message alerts");((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);}
    }

    private void notifyAdmin(String title,String message){
        Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,"vta_admin"):new Notification.Builder(this);
        b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(title).setContentText(message).setAutoCancel(true);
        ((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify((int)(System.currentTimeMillis()%100000),b.build());
    }

    // ---------- DATA / CALCULATIONS ----------

    private JSONObject findStudent(String u){JSONArray a=data.optJSONArray("students");for(int i=0;i<a.length();i++){JSONObject s=a.optJSONObject(i);if(s!=null&&u.equalsIgnoreCase(s.optString("username")))return s;}return null;}
    private JSONObject studentAt(int index){JSONArray a=data.optJSONArray("students");return index>=0&&index<a.length()?a.optJSONObject(index):null;}
    private Spinner studentSpinner(){Spinner s=new Spinner(this);JSONArray a=data.optJSONArray("students");String[] n=new String[a.length()];for(int i=0;i<a.length();i++)n[i]=a.optJSONObject(i).optString("name");s.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,n));return s;}

    private List<JSONObject> allLessonsSorted(){
        List<JSONObject> out=new ArrayList<>();JSONArray students=data.optJSONArray("students");
        for(int i=0;i<students.length();i++){JSONObject s=students.optJSONObject(i);JSONArray ls=s.optJSONArray("lessons");for(int j=0;j<ls.length();j++){JSONObject l=ls.optJSONObject(j);try{JSONObject c=new JSONObject(l.toString());c.put("_student",s.optString("name"));c.put("_status",l.optString("status"));out.add(c);}catch(Exception ignored){}}}
        Collections.sort(out,(a,b)->(a.optString("date")+a.optString("time")).compareTo(b.optString("date")+b.optString("time")));return out;
    }

    private int countUpcoming(){int n=0;for(JSONObject l:allLessonsSorted())if(!"Completed".equals(l.optString("_status"))&&! "Cancelled".equals(l.optString("_status")))n++;return n;}
    private int pendingBookings(){int n=0;JSONArray ss=data.optJSONArray("students");for(int i=0;i<ss.length();i++){JSONArray bs=ss.optJSONObject(i).optJSONArray("bookings");for(int j=0;j<bs.length();j++)if("Pending".equals(bs.optJSONObject(j).optString("status")))n++;}return n;}
    private int unreadMessages(){int n=0;JSONArray ss=data.optJSONArray("students");for(int i=0;i<ss.length();i++){JSONArray ms=ss.optJSONObject(i).optJSONArray("messages");for(int j=0;j<ms.length();j++)if(ms.optJSONObject(j).optBoolean("unread",false))n++;}return n;}
    private int completedHours(){int mins=0;for(JSONObject l:allLessonsSorted())if("Completed".equals(l.optString("_status")))mins+=l.optInt("duration");return mins/60;}
    private int invoiceTotal(){int total=0;for(JSONObject l:allLessonsSorted())if("Completed".equals(l.optString("_status")))total+=(l.optInt("duration")*RATE)/60;return total;}

    private JSONObject nextLesson(JSONObject s){
        JSONArray ls=s.optJSONArray("lessons");JSONObject best=null;String key="";
        for(int i=0;i<ls.length();i++){JSONObject l=ls.optJSONObject(i);if("Completed".equals(l.optString("status"))||"Cancelled".equals(l.optString("status")))continue;String k=l.optString("date")+l.optString("time");if(best==null||k.compareTo(key)<0){best=l;key=k;}}
        return best;
    }

    private void copyInvoiceSummary(){
        StringBuilder sb=new StringBuilder("Victoria Tuition Academy — Lesson Invoice Summary\nRate: R"+RATE+"/hour\n\n");
        for(JSONObject l:allLessonsSorted())if("Completed".equals(l.optString("_status")))sb.append(l.optString("date")).append(" — ").append(l.optString("_student")).append(" — ").append(l.optString("topic")).append(" — ").append(l.optInt("duration")).append(" min — R").append((l.optInt("duration")*RATE)/60).append("\n");
        sb.append("\nTotal: R").append(invoiceTotal());
        ((android.content.ClipboardManager)getSystemService(CLIPBOARD_SERVICE)).setPrimaryClip(ClipData.newPlainText("VTA invoice summary",sb.toString()));
        Toast.makeText(this,"Invoice summary copied",Toast.LENGTH_SHORT).show();
    }

    // ---------- UI HELPERS ----------

    private void pageTitle(String title,String subtitle){body.addView(text(title,29,NAVY,true));TextView s=text(subtitle,13,MUTED,false);s.setPadding(0,dp(4),0,dp(15));body.addView(s);}
    private void section(String title){TextView t=text(title,16,NAVY,true);t.setPadding(0,dp(18),0,dp(9));body.addView(t);}
    private LinearLayout card(){LinearLayout c=col();c.setPadding(dp(16),dp(16),dp(16),dp(16));c.setBackground(round(Color.WHITE,18,1,LINE));return c;}
    private LinearLayout emptyCard(String message){LinearLayout c=card();c.addView(text(message,13,MUTED,false));return c;}
    private LinearLayout metric(String label,String value,int accent){LinearLayout c=col();c.setPadding(dp(13),dp(13),dp(13),dp(13));c.setBackground(round(Color.WHITE,16,1,LINE));c.addView(text(value,22,accent,true));c.addView(text(label,10,MUTED,true));return c;}
    private void actionCard(String title,String subtitle,int accent,View.OnClickListener click){LinearLayout c=row();c.setGravity(Gravity.CENTER_VERTICAL);c.setPadding(dp(15),dp(14),dp(15),dp(14));c.setBackground(round(Color.WHITE,16,1,LINE));TextView d=text("•",30,accent,true);c.addView(d,new LinearLayout.LayoutParams(dp(28),dp(40)));LinearLayout t=col();t.addView(text(title,14,NAVY,true));t.addView(text(subtitle,11,MUTED,false));c.addView(t,new LinearLayout.LayoutParams(0,-2,1));c.addView(text("›",24,NAVY,false));c.setOnClickListener(click);body.addView(c,marginBottom(8));}
    private LinearLayout quick(String icon,String title,String sub,int accent,View.OnClickListener l){LinearLayout c=col();c.setPadding(dp(14),dp(14),dp(14),dp(14));c.setBackground(round(Color.WHITE,16,1,LINE));c.addView(pill(icon,light(accent),accent));c.addView(text(title,14,NAVY,true),marginTopBottom(10,2));c.addView(text(sub,11,MUTED,false));c.setOnClickListener(l);return c;}
    private LinearLayout lessonCard(JSONObject l){LinearLayout c=card();LinearLayout top=row();top.addView(text(l.optString("_student"),14,NAVY,true),new LinearLayout.LayoutParams(0,-2,1));top.addView(pill(l.optString("_status"),statusBg(l.optString("_status")),statusColor(l.optString("_status"))));c.addView(top);c.addView(text(l.optString("topic"),16,TEXT,true),marginTopBottom(8,2));c.addView(text(prettyDate(l.optString("date"))+" · "+l.optString("time")+" · "+l.optInt("duration")+" min",11,MUTED,false));c.addView(text(l.optString("subject"),11,ORANGE,true));return c;}

    private TextView pill(String s,int bg,int fg){TextView t=text(s,10,fg,true);t.setGravity(Gravity.CENTER);t.setPadding(dp(10),dp(6),dp(10),dp(6));t.setBackground(round(bg,100,0,0));return t;}
    private Button primary(String s){return button(s,ORANGE,Color.WHITE);}
    private Button secondary(String s){Button b=button(s,Color.WHITE,NAVY);b.setBackground(round(Color.WHITE,14,1,LINE));return b;}
    private Button smallButton(String s){Button b=button(s,Color.rgb(248,250,252),NAVY);b.setTextSize(11);b.setBackground(round(Color.rgb(248,250,252),12,1,LINE));return b;}
    private EditText input(String hint,boolean pass){EditText e=new EditText(this);e.setHint(hint);e.setTextSize(14);e.setTextColor(NAVY);e.setHintTextColor(Color.rgb(148,163,184));e.setPadding(dp(13),dp(11),dp(13),dp(11));e.setBackground(round(Color.WHITE,13,1,Color.rgb(203,213,225)));if(pass)e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);return e;}
    private Button button(String s,int bg,int fg){Button b=new Button(this);b.setText(s);b.setTextColor(fg);b.setTextSize(13);b.setAllCaps(false);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setBackground(round(bg,14,0,0));return b;}
    private TextView text(String s,float size,int color,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(size);t.setTextColor(color);t.setLineSpacing(0,1.06f);if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);return l;}
    private LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private LinearLayout formBox(){LinearLayout b=col();b.setPadding(dp(3),dp(8),dp(3),0);return b;}
    private GradientDrawable round(int color,int radius,int stroke,int strokeColor){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));if(stroke>0)g.setStroke(dp(stroke),strokeColor);return g;}
    private GradientDrawable gradient(int a,int b,int radius){GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a,b});g.setCornerRadius(dp(radius));return g;}
    private GradientDrawable circle(int color){GradientDrawable g=new GradientDrawable();g.setShape(GradientDrawable.OVAL);g.setColor(color);return g;}

    private LinearLayout.LayoutParams marginBottom(int m){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,dp(m));return p;}
    private LinearLayout.LayoutParams marginTopBottom(int t,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(t),0,dp(b));return p;}
    private LinearLayout.LayoutParams marginRight(int r){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,-2);p.setMargins(0,0,dp(r),0);return p;}
    private LinearLayout.LayoutParams withWeightMargin(int w,int left){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,w);p.setMargins(dp(left),0,0,0);return p;}
    private LinearLayout.LayoutParams withWeightMarginHeight(int w,int left,int h){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(h),w);p.setMargins(dp(left),0,0,0);return p;}

    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private int colorFor(int i){int[] c={ORANGE,BLUE,PURPLE,GREEN,Color.rgb(234,88,12)};return c[i%c.length];}
    private int light(int c){if(c==ORANGE)return ORANGE_SOFT;if(c==GREEN)return GREEN_SOFT;if(c==BLUE)return Color.rgb(239,246,255);return Color.rgb(245,243,255);}
    private int statusColor(String s){if("Completed".equals(s))return Color.rgb(21,128,61);if("Confirmed".equals(s))return BLUE;if("Cancelled".equals(s)||"Declined".equals(s))return Color.rgb(185,28,28);return ORANGE;}
    private int statusBg(String s){if("Completed".equals(s))return GREEN_SOFT;if("Confirmed".equals(s))return Color.rgb(239,246,255);if("Cancelled".equals(s)||"Declined".equals(s))return Color.rgb(254,242,242);return ORANGE_SOFT;}
    private String initials(String n){String[] p=n.trim().split("\\s+");return p.length==0?"?":p.length==1?p[0].substring(0,Math.min(1,p[0].length())).toUpperCase():(p[0].substring(0,1)+p[p.length-1].substring(0,1)).toUpperCase();}
    private String roleLabel(){return role.equals("ADMIN")?"Admin":role.equals("PARENT")?"Parent":"Student";}
    private String greeting(){Calendar c=Calendar.getInstance();int h=c.get(Calendar.HOUR_OF_DAY);return h<12?"Good morning":h<17?"Good afternoon":"Good evening";}
    private String today(){return new SimpleDateFormat("dd MMM yyyy",Locale.getDefault()).format(new Date());}
    private String prettyDate(String iso){try{return new SimpleDateFormat("EEE, dd MMM yyyy",Locale.ENGLISH).format(new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH).parse(iso));}catch(Exception e){return iso;}}
    private String dayShort(String iso){try{return new SimpleDateFormat("EEE",Locale.ENGLISH).format(new SimpleDateFormat("yyyy-MM-dd",Locale.ENGLISH).parse(iso)).toUpperCase();}catch(Exception e){return "";}}
    private String dayNum(String iso){return iso.length()>=10?iso.substring(8,10):iso;}
    private String endTime(String start,int mins){try{SimpleDateFormat f=new SimpleDateFormat("HH:mm",Locale.ENGLISH);Date d=f.parse(start);Calendar c=Calendar.getInstance();c.setTime(d);c.add(Calendar.MINUTE,mins);return f.format(c.getTime());}catch(Exception e){return start;}}
}
