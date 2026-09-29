package za.co.victoriatuition.booking;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.util.Base64;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class MainActivity extends Activity {

    private static final String API_URL = "https://xrmljvdyxqyegclkazei.supabase.co/functions/v1/vta-api";
    private static final int FILE_PICK = 9001;

    private static final int NAVY=Color.rgb(15,23,42), NAVY2=Color.rgb(30,41,59), ORANGE=Color.rgb(229,77,46),
        ORANGE_SOFT=Color.rgb(255,244,240), GREEN=Color.rgb(34,197,94), GREEN_SOFT=Color.rgb(240,253,244),
        BLUE=Color.rgb(37,99,235), PURPLE=Color.rgb(124,58,237), BG=Color.rgb(246,248,252),
        MUTED=Color.rgb(100,116,139), LINE=Color.rgb(226,232,240), TEXT=Color.rgb(30,41,59);

    private SharedPreferences prefs;
    private String token="", selectedRole="";
    private JSONObject sync=new JSONObject();
    private LinearLayout body;
    private JSONObject pendingStudent=null;
    private String pendingResourceTitle="", pendingResourceType="Other", pendingResourceDescription="", pendingResourceAccessNote="";
    private TextView syncStatus;
    private boolean darkMode=false;

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        getWindow().setStatusBarColor(NAVY);
        getWindow().setNavigationBarColor(Color.WHITE);
        prefs=getSharedPreferences("vta_cloud_app",MODE_PRIVATE);
        token=prefs.getString("token","");
        darkMode=prefs.getBoolean("darkMode",false);
        try{sync=new JSONObject(prefs.getString("cache","{}"));}catch(Exception ignored){}
        createNotificationChannel();
        if(!token.isEmpty()) refreshAndOpen(); else showWelcome();
    }

    // ---------- NETWORK ----------

    interface ApiCallback { void done(JSONObject result, Exception error); }

    private void api(JSONObject payload, boolean authenticated, ApiCallback cb){
        new Thread(()->{
            HttpURLConnection c=null;
            try{
                c=(HttpURLConnection)new URL(API_URL).openConnection();
                c.setRequestMethod("POST");
                c.setConnectTimeout(15000); c.setReadTimeout(30000);
                c.setDoOutput(true);
                c.setRequestProperty("Content-Type","application/json");
                if(authenticated && !token.isEmpty()) c.setRequestProperty("Authorization","Bearer "+token);
                try(OutputStream os=c.getOutputStream()){os.write(payload.toString().getBytes("UTF-8"));}
                int code=c.getResponseCode();
                InputStream is=code>=200&&code<300?c.getInputStream():c.getErrorStream();
                String text=readAll(is);
                JSONObject r=text.isEmpty()?new JSONObject():new JSONObject(text);
                if(code<200||code>=300) throw new IOException(r.optString("error","Request failed ("+code+")"));
                runOnUiThread(()->cb.done(r,null));
            }catch(Exception e){runOnUiThread(()->cb.done(null,e));}
            finally{if(c!=null)c.disconnect();}
        }).start();
    }

    private String readAll(InputStream is) throws Exception{
        if(is==null)return "";
        BufferedReader br=new BufferedReader(new InputStreamReader(is,"UTF-8"));StringBuilder sb=new StringBuilder();String line;
        while((line=br.readLine())!=null)sb.append(line);return sb.toString();
    }

    private void refreshAndOpen(){
        JSONObject p=new JSONObject();try{p.put("action","sync");}catch(Exception ignored){}
        api(p,true,(r,e)->{
            if(e!=null){
                if(sync.length()>0){showDashboard();toast("Offline — showing cached data");}
                else {token="";prefs.edit().remove("token").apply();showWelcome();toast("Could not connect. Please sign in again.");}
                return;
            }
            sync=r;cache();showDashboard();showNewNotifications();
        });
    }

    private void refreshCurrent(){
        if(syncStatus!=null)syncStatus.setText("Syncing…");
        JSONObject p=new JSONObject();try{p.put("action","sync");}catch(Exception ignored){}
        api(p,true,(r,e)->{
            if(e!=null){if(syncStatus!=null)syncStatus.setText("Offline");toast(e.getMessage());return;}
            sync=r;cache();if(syncStatus!=null)syncStatus.setText("Live");openTab(currentTab());
        });
    }

    private void cache(){prefs.edit().putString("cache",sync.toString()).apply();}
    private String currentTab(){return prefs.getString("tab","Home");}
    private void setTab(String t){prefs.edit().putString("tab",t).apply();}

    private void action(JSONObject p, Runnable onSuccess){
        api(p,true,(r,e)->{
            if(e!=null){toast(e.getMessage());return;}
            JSONObject s=new JSONObject();try{s.put("action","sync");}catch(Exception ignored){}
            api(s,true,(rr,ee)->{
                if(ee==null){sync=rr;cache();}
                if(onSuccess!=null)onSuccess.run();
            });
        });
    }

    // ---------- LOGIN ----------

    private void showWelcome(){
        ScrollView sv=new ScrollView(this);LinearLayout root=col();root.setPadding(dp(20),dp(26),dp(20),dp(30));root.setBackgroundColor(BG);sv.addView(root);setContentView(sv);
        LinearLayout brand=row();brand.setGravity(Gravity.CENTER_VERTICAL);
        ImageView logo=new ImageView(this);int brandId=getResources().getIdentifier("vta"+"_"+"brand"+"_"+"mark","drawable",getPackageName());logo.setImageResource(brandId);logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);brand.addView(logo,new LinearLayout.LayoutParams(dp(58),dp(58)));
        LinearLayout bt=col();bt.setPadding(dp(12),0,0,0);bt.addView(text("Victoria Tuition Academy",19,NAVY,true));bt.addView(text("Better Understanding. Better Results.",11,MUTED,false));brand.addView(bt);root.addView(brand);

        LinearLayout hero=col();hero.setPadding(dp(22),dp(24),dp(22),dp(24));hero.setBackground(gradient(NAVY,NAVY2,22));
        hero.addView(pill("PRIVATE LEARNING PORTAL",Color.rgb(51,65,85),Color.WHITE));
        TextView h=text("Everything for tutoring,\nin one app.",31,Color.WHITE,true);h.setPadding(0,dp(16),0,dp(10));hero.addView(h);
        hero.addView(text("Live lessons, bookings, resources and tutor communication.",14,Color.rgb(203,213,225),false));
        root.addView(hero,marginTopBottom(28,22));

        root.addView(text("Continue as",18,NAVY,true));
        TextView hint=text("Choose your role. Your username still determines what you can access.",12,MUTED,false);hint.setPadding(0,dp(4),0,dp(14));root.addView(hint);
        roleCard(root,"Admin","Run your tutoring week, students, finances and resources.","ADMIN",ORANGE);
        roleCard(root,"Student","Your lessons, bookings, resources and tutor messages.","STUDENT",BLUE);
        roleCard(root,"Parent","View the learner’s lessons and communication.","PARENT",GREEN);
    }

    private void roleCard(LinearLayout root,String title,String sub,String role,int accent){
        LinearLayout c=row();c.setGravity(Gravity.CENTER_VERTICAL);c.setPadding(dp(16),dp(16),dp(14),dp(16));c.setBackground(round(Color.WHITE,18,1,LINE));
        TextView dot=text("•",34,accent,true);dot.setGravity(Gravity.CENTER);c.addView(dot,new LinearLayout.LayoutParams(dp(34),dp(44)));
        LinearLayout t=col();t.addView(text(title,17,NAVY,true));t.addView(text(sub,12,MUTED,false));c.addView(t,new LinearLayout.LayoutParams(0,-2,1));c.addView(text("›",28,NAVY,false));
        c.setOnClickListener(v->{selectedRole=role;showLogin();});root.addView(c,marginBottom(10));
    }

    private void showLogin(){
        ScrollView sv=new ScrollView(this);LinearLayout root=col();root.setPadding(dp(20),dp(24),dp(20),dp(30));root.setBackgroundColor(BG);sv.addView(root);setContentView(sv);
        TextView back=text("‹  Back",15,NAVY,true);back.setPadding(0,dp(6),0,dp(24));back.setOnClickListener(v->showWelcome());root.addView(back);
        root.addView(pill(selectedRole+" ACCESS",ORANGE_SOFT,ORANGE));
        TextView title=text(selectedRole.equals("ADMIN")?"Welcome back, Victoria":"Welcome back",32,NAVY,true);title.setPadding(0,dp(14),0,dp(6));root.addView(title);
        TextView sub=text("Sign in to your live VTA workspace.",14,MUTED,false);sub.setPadding(0,0,0,dp(22));root.addView(sub);

        LinearLayout card=card();EditText user=input("Username",false),pass=input("Password",true);card.addView(user);card.addView(pass,marginTopBottom(12,0));
        TextView error=text("",12,Color.rgb(185,28,28),true);error.setPadding(0,dp(8),0,0);card.addView(error);
        Button login=primary("Sign in securely");card.addView(login,marginTopBottom(14,0));root.addView(card);

        login.setOnClickListener(v->{
            login.setEnabled(false);login.setText("Signing in…");error.setText("");
            JSONObject p=new JSONObject();try{p.put("action","login");p.put("username",user.getText().toString().trim());p.put("password",pass.getText().toString());}catch(Exception ignored){}
            api(p,false,(r,e)->{
                login.setEnabled(true);login.setText("Sign in securely");
                if(e!=null){error.setText(e.getMessage());return;}
                JSONObject u=r.optJSONObject("user");
                if(u==null||(!selectedRole.isEmpty()&&!selectedRole.equals(u.optString("role")))){
                    error.setText("This account is not a "+selectedRole.toLowerCase()+" account.");return;
                }
                token=r.optString("token");prefs.edit().putString("token",token).apply();refreshAndOpen();
            });
        });
    }

    // ---------- SHELL ----------

    private void showDashboard(){
        JSONObject user=sync.optJSONObject("user");if(user==null){showWelcome();return;}
        int screenBg=themeColor(BG), surface=themeColor(Color.WHITE), primaryText=themeColor(NAVY);
        getWindow().setNavigationBarColor(surface);

        LinearLayout root=col();root.setBackgroundColor(screenBg);
        LinearLayout header=row();header.setGravity(Gravity.CENTER_VERTICAL);header.setPadding(dp(16),dp(11),dp(12),dp(11));header.setBackgroundColor(NAVY);

        ImageView brandMark=new ImageView(this);int brandRes=getResources().getIdentifier("vta"+"_"+"brand"+"_"+"mark","drawable",getPackageName());brandMark.setImageResource(brandRes);brandMark.setScaleType(ImageView.ScaleType.CENTER_INSIDE);header.addView(brandMark,new LinearLayout.LayoutParams(dp(42),dp(42)));
        LinearLayout brand=col();brand.setPadding(dp(9),0,0,0);
        brand.addView(text("Victoria Tuition Academy",15,Color.WHITE,true));
        brand.addView(text("LEARN • GROW • ACHIEVE",8,Color.rgb(203,213,225),true));
        header.addView(brand,new LinearLayout.LayoutParams(0,-2,1));

        syncStatus=text("● Live",10,Color.rgb(134,239,172),true);header.addView(syncStatus);
        TextView mode=pill(darkMode?"Light":"Dark",Color.rgb(51,65,85),Color.WHITE);
        mode.setOnClickListener(v->{darkMode=!darkMode;prefs.edit().putBoolean("darkMode",darkMode).apply();showDashboard();});
        header.addView(mode,marginLeft(8));
        TextView refresh=pill("↻",Color.rgb(51,65,85),Color.WHITE);refresh.setOnClickListener(v->refreshCurrent());header.addView(refresh,marginLeft(8));
        root.addView(header);

        ScrollView sv=new ScrollView(this);sv.setFillViewport(true);
        body=col();body.setPadding(dp(16),dp(16),dp(16),dp(28));body.setBackgroundColor(screenBg);sv.addView(body);
        root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));

        LinearLayout nav=row();nav.setGravity(Gravity.CENTER);nav.setPadding(dp(5),dp(5),dp(5),dp(5));nav.setBackgroundColor(surface);
        String role=user.optString("role");
        String[] tabs=role.equals("ADMIN")?new String[]{"Home","Students","Schedule","Messages","More"}:new String[]{"Home","Learn","Book","Messages","More"};
        String[] icons=role.equals("ADMIN")?new String[]{"⌂","◎","▦","●","•••"}:new String[]{"⌂","◇","+","●","•••"};
        for(int i=0;i<tabs.length;i++){
            final String t=tabs[i];
            LinearLayout item=col();item.setGravity(Gravity.CENTER);item.setPadding(dp(6),dp(4),dp(6),dp(4));
            TextView ic=text(icons[i],18,primaryText,true), label=text(t,10,MUTED,true);
            item.addView(ic);item.addView(label);
            item.setOnClickListener(v->{setTab(t);openTab(t);});
            nav.addView(item,new LinearLayout.LayoutParams(0,dp(58),1));
        }
        root.addView(nav);setContentView(root);openTab(currentTab());
    }

    private void openTab(String tab){
        if(body==null)return;body.removeAllViews();JSONObject u=sync.optJSONObject("user");if(u==null)return;
        if(u.optString("role").equals("ADMIN")){
            switch(tab){case "Students":adminStudents();break;case "Schedule":adminSchedule();break;case "Messages":adminMessages();break;case "More":adminMore();break;case "Finance":adminFinance();break;case "Resources":adminResources();break;case "Format":adminFormat();break;default:adminHome();}
        }else{
            switch(tab){case "Learn":studentLearning();break;case "Book":studentBook();break;case "Messages":studentMessages();break;case "More":studentMore();break;case "Lessons":studentLessons();break;case "Resources":studentResources();break;default:studentHome();}
        }
    }

    private void adminMore(){
        pageTitle("More","Finance, resources and app settings.");
        LinearLayout finance=card();finance.addView(text("Finance & invoices",16,NAVY,true));finance.addView(text("Payments, completed lesson value and invoice drafts.",12,MUTED,false));finance.setOnClickListener(v->openTab("Finance"));body.addView(finance,marginBottom(10));
        LinearLayout resources=card();resources.addView(text("Resource library",16,NAVY,true));resources.addView(text("Interactive lessons, slides, notes, worksheets, videos and images.",12,MUTED,false));resources.setOnClickListener(v->openTab("Resources"));body.addView(resources,marginBottom(10));
        LinearLayout appearance=card();appearance.addView(text("Appearance",16,NAVY,true));appearance.addView(text(darkMode?"Dark mode is on":"Light mode is on",12,MUTED,false));appearance.setOnClickListener(v->{darkMode=!darkMode;prefs.edit().putBoolean("darkMode",darkMode).apply();showDashboard();});body.addView(appearance,marginBottom(10));
        LinearLayout out=card();out.addView(text("Log out",16,Color.rgb(185,28,28),true));out.setOnClickListener(v->logout());body.addView(out);
    }

    private void studentMore(){
        pageTitle("More","Your lessons, resources and preferences.");
        LinearLayout lessons=card();lessons.addView(text("Lesson history",16,NAVY,true));lessons.addView(text("Upcoming, completed and requested lessons.",12,MUTED,false));lessons.setOnClickListener(v->openTab("Lessons"));body.addView(lessons,marginBottom(10));
        LinearLayout resources=card();resources.addView(text("All resources",16,NAVY,true));resources.addView(text("Open your interactive lessons, slides, notes and worksheets.",12,MUTED,false));resources.setOnClickListener(v->openTab("Resources"));body.addView(resources,marginBottom(10));
        LinearLayout appearance=card();appearance.addView(text("Appearance",16,NAVY,true));appearance.addView(text(darkMode?"Switch to light mode":"Switch to dark mode",12,MUTED,false));appearance.setOnClickListener(v->{darkMode=!darkMode;prefs.edit().putBoolean("darkMode",darkMode).apply();showDashboard();});body.addView(appearance,marginBottom(10));
        LinearLayout out=card();out.addView(text("Log out",16,Color.rgb(185,28,28),true));out.setOnClickListener(v->logout());body.addView(out);
    }

    private void logout(){token="";sync=new JSONObject();prefs.edit().clear().apply();showWelcome();}

    // ---------- ADMIN HOME ----------

    private void adminHome(){
        JSONObject u=sync.optJSONObject("user");
        pageTitle(greeting()+", "+u.optString("displayName"),"Your tutoring week, without the clutter.");
        int students=sync.optJSONArray("students").length(), pending=countWhere(sync.optJSONArray("bookings"),"status","Pending"), alerts=sync.optJSONArray("notifications").length();

        LinearLayout hero=card();hero.setPadding(dp(20),dp(20),dp(20),dp(20));hero.setBackground(gradient(NAVY,Color.rgb(31,55,96),22));
        hero.addView(text("WEEK OVERVIEW",10,Color.rgb(148,163,184),true));
        hero.addView(text(upcomingLessons()+" lessons ahead",28,Color.WHITE,true),marginTopBottom(7,3));
        hero.addView(text("R"+invoiceReadyTotal()+" completed lesson value ready for records",12,Color.rgb(203,213,225),false));
        LinearLayout hstats=row();hstats.setPadding(0,dp(16),0,0);
        hstats.addView(pill(students+" students",Color.rgb(38,58,91),Color.WHITE));
        hstats.addView(pill(pending+" requests",Color.rgb(68,46,42),Color.rgb(255,205,190)),marginLeft(8));
        hstats.addView(pill(alerts+" alerts",Color.rgb(28,70,55),Color.rgb(187,247,208)),marginLeft(8));
        hero.addView(hstats);body.addView(hero,marginBottom(16));

        section("Next lessons");
        JSONArray ls=sync.optJSONArray("lessons");int shown=0;
        for(int i=0;i<ls.length()&&shown<4;i++){JSONObject l=ls.optJSONObject(i);if(!"Completed".equals(l.optString("status"))&&!"Cancelled".equals(l.optString("status"))){body.addView(adminLessonCard(l),marginBottom(8));shown++;}}
        if(shown==0)body.addView(empty("No upcoming lessons recorded."));

        if(pending>0){
            section("Needs your attention");
            LinearLayout request=card();request.addView(text(pending+" booking request"+(pending==1?"":"s"),17,ORANGE,true));request.addView(text("Review requested dates, topics and times.",11,MUTED,false),marginTopBottom(4,8));
            Button open=secondary("Review bookings");open.setOnClickListener(v->{setTab("Schedule");openTab("Schedule");});request.addView(open);body.addView(request,marginBottom(8));
        }

        section("Quick actions");
        LinearLayout q=row();q.addView(quick("＋","Add student","Create portal",ORANGE,v->dialogCreateStudent()),new LinearLayout.LayoutParams(0,-2,1));q.addView(quick("▣","Log lesson","Teaching record",BLUE,v->dialogAddLesson()),weightMargin(1,8));body.addView(q,marginBottom(8));
        LinearLayout q2=row();q2.addView(quick("↑","Upload","Student resource",PURPLE,v->chooseStudentForResource()),new LinearLayout.LayoutParams(0,-2,1));q2.addView(quick("●","Update","Announcement",GREEN,v->dialogAnnouncement()),weightMargin(1,8));body.addView(q2,marginBottom(14));

        LinearLayout finance=card();LinearLayout ft=row();ft.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout fi=col();fi.addView(text("Finance",15,NAVY,true));fi.addView(text("Completed lesson value · R"+invoiceReadyTotal(),11,MUTED,false));ft.addView(fi,new LinearLayout.LayoutParams(0,-2,1));ft.addView(text("›",25,NAVY,false));finance.addView(ft);
        finance.setOnClickListener(v->openTab("Finance"));body.addView(finance);
    }

    private void adminStudents(){
        pageTitle("Students","Profiles, syllabus progress and learning plans.");
        Button add=primary("＋ Create student portal");add.setOnClickListener(v->dialogCreateStudent());body.addView(add,marginBottom(14));
        JSONArray students=sync.optJSONArray("students"),plans=sync.optJSONArray("plans"),topics=sync.optJSONArray("topicProgress");
        for(int i=0;i<students.length();i++){
            JSONObject s=students.optJSONObject(i),plan=findBy(plans,"student_id",s.optString("id"));LinearLayout c=card();
            LinearLayout top=row();top.setGravity(Gravity.CENTER_VERTICAL);
            TextView av=text(initials(s.optString("display_name")),15,Color.WHITE,true);av.setGravity(Gravity.CENTER);av.setBackground(circle(colorFor(i)));top.addView(av,new LinearLayout.LayoutParams(dp(46),dp(46)));
            LinearLayout t=col();t.setPadding(dp(12),0,0,0);t.addView(text(s.optString("display_name"),18,NAVY,true));t.addView(text(nz(s.optString("grade"))+" · "+nz(s.optString("curriculum")),11,MUTED,false));top.addView(t,new LinearLayout.LayoutParams(0,-2,1));
            top.addView(text("›",26,NAVY,false));c.addView(top);
            c.addView(text(nz(s.optString("subjects")),13,TEXT,false),marginTopBottom(10,6));
            int total=0,covered=0,next=0;
            for(int j=0;j<topics.length();j++){JSONObject x=topics.optJSONObject(j);if(!s.optString("id").equals(x.optString("student_id")))continue;total++;String st=x.optString("status");if("Covered".equals(st)||"Completed".equals(st))covered++;if("Next".equals(st)||"Current".equals(st))next++;}
            c.addView(text(covered+" covered  •  "+next+" current/next  •  "+total+" syllabus topics",11,MUTED,false));
            if(plan!=null)c.addView(text(plan.optInt("lesson_target")+" lessons · "+plan.optString("month_key"),11,GREEN,true),marginTopBottom(6,0));
            c.setOnClickListener(v->adminStudentProfile(s));
            body.addView(c,marginBottom(10));
        }
    }

    private void adminStudentProfile(JSONObject s){
        body.removeAllViews();
        TextView back=text("‹  Students",14,NAVY,true);back.setPadding(0,0,0,dp(14));back.setOnClickListener(v->adminStudents());body.addView(back);
        LinearLayout hero=card();hero.setBackground(gradient(NAVY,Color.rgb(31,55,96),20));
        hero.addView(text(s.optString("display_name"),28,Color.WHITE,true));
        hero.addView(text(nz(s.optString("grade"))+" · "+nz(s.optString("curriculum"))+" · "+nz(s.optString("subjects")),12,Color.rgb(203,213,225),false));
        if(!s.optString("profile_note").isEmpty())hero.addView(text(s.optString("profile_note"),11,Color.rgb(203,213,225),false),marginTopBottom(8,0));
        body.addView(hero,marginBottom(12));

        LinearLayout actions=row();
        Button profile=secondary("Edit profile");profile.setOnClickListener(v->dialogStudentProfile(s));actions.addView(profile,new LinearLayout.LayoutParams(0,dp(46),1));
        JSONObject plan=findBy(sync.optJSONArray("plans"),"student_id",s.optString("id"));
        Button planBtn=secondary("Monthly plan");planBtn.setOnClickListener(v->dialogPlan(s,plan));actions.addView(planBtn,weightMarginHeight(1,8,46));
        body.addView(actions,marginBottom(10));

        LinearLayout actions2=row();
        Button addTopic=primary("＋ Add syllabus topic");addTopic.setOnClickListener(v->dialogTopic(s,null));actions2.addView(addTopic,new LinearLayout.LayoutParams(0,dp(46),1));
        Button msg=secondary("Message");msg.setOnClickListener(v->dialogMessage(s.optString("id")));actions2.addView(msg,weightMarginHeight(1,8,46));
        body.addView(actions2,marginBottom(14));

        section("Syllabus & progress");
        JSONArray topics=sync.optJSONArray("topicProgress");int count=0;
        for(int i=0;i<topics.length();i++){
            JSONObject x=topics.optJSONObject(i);if(!s.optString("id").equals(x.optString("student_id")))continue;count++;
            LinearLayout row=card();LinearLayout top=row();top.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout info=col();info.addView(text(x.optString("topic"),14,NAVY,true));info.addView(text(x.optString("term_label")+" · "+x.optString("strand"),10,MUTED,false));top.addView(info,new LinearLayout.LayoutParams(0,-2,1));
            top.addView(pill(x.optString("status"),statusBg(x.optString("status")),statusColor(x.optString("status"))));row.addView(top);
            if(!x.optString("notes").isEmpty())row.addView(text(x.optString("notes"),11,MUTED,false),marginTopBottom(7,0));
            row.setOnClickListener(v->dialogTopic(s,x));body.addView(row,marginBottom(8));
        }
        if(count==0)body.addView(empty("No syllabus topics yet. Add the first topic above."));
    }

    private void dialogStudentProfile(JSONObject s){
        LinearLayout b=formBox();
        EditText name=input("Student name",false),grade=input("Grade",false),curr=input("Curriculum",false),subjects=input("Subjects",false),note=input("Profile note",false);
        name.setText(s.optString("display_name"));grade.setText(s.optString("grade"));curr.setText(s.optString("curriculum"));subjects.setText(s.optString("subjects"));note.setText(s.optString("profile_note"));
        for(EditText e:new EditText[]{name,grade,curr,subjects,note})b.addView(e,marginBottom(8));
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Edit student profile").setView(b).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();
        d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            JSONObject q=new JSONObject();try{q.put("action","studentUpdate");q.put("studentId",s.optString("id"));q.put("name",name.getText().toString());q.put("grade",grade.getText().toString());q.put("curriculum",curr.getText().toString());q.put("subjects",subjects.getText().toString());q.put("profileNote",note.getText().toString());q.put("active",true);}catch(Exception ignored){}
            action(q,()->{d.dismiss();JSONObject updated=findBy(sync.optJSONArray("students"),"id",s.optString("id"));adminStudentProfile(updated==null?s:updated);});
        }));d.show();
    }

    private void dialogTopic(JSONObject student,JSONObject topic){
        LinearLayout b=formBox();
        EditText subject=input("Subject",false),strand=input("Strand / section",false),term=input("Term",false),name=input("Topic",false),notes=input("Tutor notes / what is next",false);
        Spinner status=new Spinner(this);String[] statuses={"Covered","Current","Next","Revisit","Future","Resource Ready"};status.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,statuses));
        EditText order=input("Sort order",false);order.setInputType(InputType.TYPE_CLASS_NUMBER);
        if(topic!=null){subject.setText(topic.optString("subject"));strand.setText(topic.optString("strand"));term.setText(topic.optString("term_label"));name.setText(topic.optString("topic"));notes.setText(topic.optString("notes"));order.setText(String.valueOf(topic.optInt("sort_order")));for(int i=0;i<statuses.length;i++)if(statuses[i].equals(topic.optString("status")))status.setSelection(i);}
        else{subject.setText(student.optString("subjects"));order.setText("999");}
        for(EditText e:new EditText[]{subject,strand,term,name})b.addView(e,marginBottom(8));b.addView(status,marginBottom(8));b.addView(notes,marginBottom(8));b.addView(order);
        AlertDialog d=new AlertDialog.Builder(this).setTitle(topic==null?"Add syllabus topic":"Edit syllabus topic").setView(b).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();
        d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            if(name.getText().toString().trim().isEmpty()){toast("Enter a topic name.");return;}
            JSONObject q=new JSONObject();try{q.put("action","topicUpsert");q.put("studentId",student.optString("id"));if(topic!=null)q.put("id",topic.optString("id"));q.put("subject",subject.getText().toString());q.put("strand",strand.getText().toString());q.put("termLabel",term.getText().toString());q.put("topic",name.getText().toString());q.put("status",status.getSelectedItem().toString());q.put("notes",notes.getText().toString());q.put("sortOrder",Integer.parseInt(order.getText().toString().trim().isEmpty()?"999":order.getText().toString()));}catch(Exception ignored){}
            action(q,()->{d.dismiss();JSONObject updated=findBy(sync.optJSONArray("students"),"id",student.optString("id"));adminStudentProfile(updated==null?student:updated);});
        }));d.show();
        if(topic!=null){
            Button del=secondary("Delete topic");
            del.setTextColor(Color.rgb(185,28,28));
            del.setOnClickListener(v->new AlertDialog.Builder(this).setTitle("Delete topic?").setMessage(topic.optString("topic")).setNegativeButton("Cancel",null).setPositiveButton("Delete",(x,w)->{
                JSONObject q=new JSONObject();try{q.put("action","topicDelete");q.put("id",topic.optString("id"));}catch(Exception ignored){}
                action(q,()->{d.dismiss();JSONObject updated=findBy(sync.optJSONArray("students"),"id",student.optString("id"));adminStudentProfile(updated==null?student:updated);});
            }).show());
            b.addView(del,marginTopBottom(10,0));
        }
    }

    private void adminSchedule(){
        pageTitle("Schedule","Booking requests and your private shared availability.");
        LinearLayout actions=row();Button lesson=primary("＋ Lesson");lesson.setOnClickListener(v->dialogAddLesson());actions.addView(lesson,new LinearLayout.LayoutParams(0,dp(48),1));Button block=secondary("＋ Time block");block.setOnClickListener(v->dialogScheduleBlock());actions.addView(block,weightMarginHeight(1,8,48));body.addView(actions,marginBottom(14));

        section("Booking requests");
        JSONArray b=sync.optJSONArray("bookings"),students=sync.optJSONArray("students");int p=0;
        for(int i=0;i<b.length();i++){JSONObject x=b.optJSONObject(i);if("Pending".equals(x.optString("status"))){p++;body.addView(bookingRequestCard(x,studentName(x.optString("student_id"))),marginBottom(8));}}
        if(p==0)body.addView(empty("No pending requests."));

        section("Lessons");
        JSONArray ls=sync.optJSONArray("lessons");
        for(int i=0;i<ls.length();i++)body.addView(adminLessonCard(ls.optJSONObject(i)),marginBottom(8));

        section("Private schedule blocks");
        JSONArray sc=sync.optJSONArray("schedule");
        for(int i=0;i<sc.length();i++){JSONObject e=sc.optJSONObject(i);LinearLayout c=card();c.addView(text(e.optString("event_date")+" · "+shortTime(e.optString("start_time"))+"–"+shortTime(e.optString("end_time")),13,ORANGE,true));c.addView(text(e.optString("title"),15,NAVY,true));c.addView(text("Students only see this period as unavailable.",11,MUTED,false));body.addView(c,marginBottom(8));}
    }

    private void adminFinance(){
        pageTitle("Finance & invoices","Completed lesson value, payments and invoice-ready records.");
        LinearLayout hero=card();hero.setBackground(gradient(NAVY,NAVY2,20));hero.addView(text("INVOICE-READY VALUE",10,Color.rgb(148,163,184),true));hero.addView(text("R"+invoiceReadyTotal(),31,Color.WHITE,true));hero.addView(text("Based on completed lessons at R150/hour",12,Color.rgb(203,213,225),false));body.addView(hero,marginBottom(14));
        LinearLayout acts=row();Button pay=secondary("＋ Payment");pay.setOnClickListener(v->dialogPayment());acts.addView(pay,new LinearLayout.LayoutParams(0,dp(46),1));Button inv=primary("Create invoice");inv.setOnClickListener(v->dialogInvoice());acts.addView(inv,weightMarginHeight(1,8,46));body.addView(acts,marginBottom(14));

        section("Payments");
        JSONArray ps=sync.optJSONArray("payments");for(int i=0;i<ps.length();i++){JSONObject p=ps.optJSONObject(i);LinearLayout c=card();c.addView(text(p.optString("payer"),15,NAVY,true));c.addView(text(p.optString("payment_date"),11,MUTED,false));Object cents=p.opt("amount_cents");c.addView(text(cents==null||cents==JSONObject.NULL?"Amount not entered":"R"+(p.optLong("amount_cents")/100),16,cents==null||cents==JSONObject.NULL?ORANGE:GREEN,true));c.addView(text(p.optString("note"),11,MUTED,false));body.addView(c,marginBottom(8));}

        section("Invoices");
        JSONArray invs=sync.optJSONArray("invoices");if(invs.length()==0)body.addView(empty("No invoices created yet."));
        for(int i=0;i<invs.length();i++){JSONObject v=invs.optJSONObject(i);LinearLayout c=card();c.addView(text(v.optString("invoice_number"),15,NAVY,true));c.addView(text(v.optString("period_start")+" → "+v.optString("period_end"),11,MUTED,false));c.addView(text("R"+(v.optLong("total_cents")/100)+" · "+v.optString("status"),14,GREEN,true));body.addView(c,marginBottom(8));}
    }

    private void adminMessages(){
        pageTitle("Messages","Student conversations.");
        Button a=secondary("＋ Post announcement");a.setOnClickListener(v->dialogAnnouncement());body.addView(a,marginBottom(14));
        JSONArray students=sync.optJSONArray("students"),ms=sync.optJSONArray("messages");
        for(int i=0;i<students.length();i++){
            JSONObject s=students.optJSONObject(i),last=null;
            for(int j=0;j<ms.length();j++){JSONObject m=ms.optJSONObject(j);if(s.optString("id").equals(m.optString("student_id"))){last=m;break;}}
            LinearLayout row=card();row.setPadding(dp(12),dp(12),dp(12),dp(12));LinearLayout top=row();top.setGravity(Gravity.CENTER_VERTICAL);
            TextView av=text(initials(s.optString("display_name")),14,Color.WHITE,true);av.setGravity(Gravity.CENTER);av.setBackground(circle(colorFor(i)));top.addView(av,new LinearLayout.LayoutParams(dp(44),dp(44)));
            LinearLayout info=col();info.setPadding(dp(11),0,0,0);info.addView(text(s.optString("display_name"),15,NAVY,true));info.addView(text(last==null?"No messages yet":last.optString("body"),11,MUTED,false));top.addView(info,new LinearLayout.LayoutParams(0,-2,1));
            if(last!=null){String ts=last.optString("created_at");if(ts.length()>=16)ts=ts.substring(11,16);top.addView(text(ts,10,MUTED,false));}
            row.addView(top);row.setOnClickListener(v->adminChat(s.optString("id")));body.addView(row,marginBottom(8));
        }
        section("Announcements");JSONArray an=sync.optJSONArray("announcements");for(int i=0;i<Math.min(3,an.length());i++){JSONObject x=an.optJSONObject(i);LinearLayout card=card();card.addView(text(x.optString("title"),14,NAVY,true));card.addView(text(x.optString("body"),11,MUTED,false),marginTopBottom(4,0));body.addView(card,marginBottom(8));}
    }

    private void adminChat(String studentId){
        body.removeAllViews();TextView back=text("‹  Messages",14,NAVY,true);back.setPadding(0,0,0,dp(12));back.setOnClickListener(v->adminMessages());body.addView(back);
        pageTitle(studentName(studentId),"Tutor chat");
        JSONArray ms=sync.optJSONArray("messages");String me=sync.optJSONObject("user").optString("id");int count=0;
        for(int i=ms.length()-1;i>=0;i--){JSONObject m=ms.optJSONObject(i);if(!studentId.equals(m.optString("student_id")))continue;count++;body.addView(chatBubble(m,me.equals(m.optString("sender_id"))),marginBottom(6));}
        if(count==0)body.addView(empty("No messages yet."));
        LinearLayout composer=row();composer.setGravity(Gravity.CENTER_VERTICAL);composer.setPadding(dp(6),dp(6),dp(6),dp(6));composer.setBackground(round(Color.WHITE,22,1,LINE));
        EditText msg=input("Message "+studentName(studentId),false);msg.setSingleLine(false);msg.setMaxLines(4);composer.addView(msg,new LinearLayout.LayoutParams(0,-2,1));
        TextView send=text("➤",19,Color.WHITE,true);send.setGravity(Gravity.CENTER);send.setBackground(circle(Color.rgb(0,168,132)));composer.addView(send,new LinearLayout.LayoutParams(dp(44),dp(44)));
        send.setOnClickListener(v->{String value=msg.getText().toString().trim();if(value.isEmpty())return;msg.setText("");JSONObject q=new JSONObject();try{q.put("action","messageSend");q.put("studentId",studentId);q.put("message",value);}catch(Exception ignored){}action(q,()->adminChat(studentId));});
        body.addView(composer,marginTopBottom(12,0));
    }

    private void adminResources(){
        pageTitle("Resource library","Organised by type so the page stays clean.");
        Button up=primary("↑ Upload from phone");up.setOnClickListener(v->chooseStudentForResource());body.addView(up,marginBottom(14));
        String[] types={"Interactive Lesson","Slides","PDF / Notes","Worksheet","Video","Image","Other"};
        String[] labels={"Interactive lessons","Slides","Notes & PDFs","Worksheets","Videos","Images","Other"};
        JSONArray rs=sync.optJSONArray("resources");
        for(int i=0;i<types.length;i++){
            String type=types[i];int count=0;
            for(int j=0;j<rs.length();j++)if(type.equals(rs.optJSONObject(j).optString("resource_type")))count++;
            if(count==0&&!"Interactive Lesson".equals(type))continue;
            LinearLayout c=card();LinearLayout top=row();top.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout info=col();info.addView(text(labels[i],16,NAVY,true));info.addView(text(count+" resource"+(count==1?"":"s"),11,MUTED,false));top.addView(info,new LinearLayout.LayoutParams(0,-2,1));top.addView(text("›",26,NAVY,false));c.addView(top);
            c.setOnClickListener(v->adminResourceCategory(type));body.addView(c,marginBottom(9));
        }
    }

    private void adminResourceCategory(String type){
        body.removeAllViews();TextView back=text("‹  Resource library",14,NAVY,true);back.setPadding(0,0,0,dp(14));back.setOnClickListener(v->adminResources());body.addView(back);
        pageTitle(type,"Open, preview or edit each resource.");
        JSONArray rs=sync.optJSONArray("resources");int count=0;
        for(int i=0;i<rs.length();i++){
            JSONObject r=rs.optJSONObject(i);if(!type.equals(r.optString("resource_type")))continue;count++;
            LinearLayout c=card();c.addView(text(r.optString("title"),16,NAVY,true));c.addView(text(studentName(r.optString("student_id")),11,MUTED,true),marginTopBottom(3,0));
            if(!r.optString("description").isEmpty())c.addView(text(r.optString("description"),11,MUTED,false),marginTopBottom(6,0));
            if(!r.optString("access_note").isEmpty())c.addView(text(r.optString("access_note"),11,ORANGE,true),marginTopBottom(7,0));
            LinearLayout a=row();Button open=smallButton("Open / preview");open.setOnClickListener(v->downloadResource(r));a.addView(open,new LinearLayout.LayoutParams(0,dp(44),1));
            Button edit=smallButton("Edit");edit.setOnClickListener(v->dialogResourceEdit(r,type));a.addView(edit,weightMarginHeight(1,8,44));c.addView(a,marginTopBottom(10,0));body.addView(c,marginBottom(8));
        }
        if(count==0)body.addView(empty("No resources in this category yet."));
    }

    private void dialogResourceEdit(JSONObject r,String returnType){
        LinearLayout b=formBox();
        EditText title=input("Title",false),desc=input("Description",false),access=input("Access note / lesson password",false);
        title.setText(r.optString("title"));desc.setText(r.optString("description"));access.setText(r.optString("access_note"));
        Spinner type=new Spinner(this);String[] types={"Interactive Lesson","Slides","PDF / Notes","Worksheet","Video","Image","Other"};type.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,types));for(int i=0;i<types.length;i++)if(types[i].equals(r.optString("resource_type")))type.setSelection(i);
        Spinner status=new Spinner(this);String[] statuses={"Available","Completed","Current","Archived"};status.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,statuses));for(int i=0;i<statuses.length;i++)if(statuses[i].equals(r.optString("completion_status")))status.setSelection(i);
        b.addView(title,marginBottom(8));b.addView(type,marginBottom(8));b.addView(desc,marginBottom(8));b.addView(access,marginBottom(8));b.addView(status);
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Edit resource").setView(b).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();
        d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            JSONObject q=new JSONObject();try{q.put("action","resourceUpdate");q.put("id",r.optString("id"));q.put("title",title.getText().toString());q.put("resourceType",type.getSelectedItem().toString());q.put("description",desc.getText().toString());q.put("accessNote",access.getText().toString());q.put("completionStatus",status.getSelectedItem().toString());q.put("featured",r.optBoolean("featured"));}catch(Exception ignored){}
            action(q,()->{d.dismiss();adminResourceCategory(type.getSelectedItem().toString());});
        }));d.show();
    }

    private void adminFormat(){
        pageTitle("App format","These settings sync to every app.");
        JSONObject settings=settingsMap();
        settingCard("Lesson display",settings.optString("lesson_format","Detailed cards"),new String[]{"Detailed cards","Compact cards"},"lesson_format");
        settingCard("Resource display",settings.optString("resource_format","Preview cards"),new String[]{"Preview cards","Compact list"},"resource_format");
        settingCard("Booking spacing",settings.optString("slot_interval","30")+" minutes",new String[]{"30","60"},"slot_interval");
    }

    // ---------- STUDENT ----------

    private void studentHome(){
        JSONObject u=sync.optJSONObject("user");pageTitle("Hi, "+u.optString("displayName"),u.optString("grade")+" · "+u.optString("curriculum")+" · "+u.optString("subjects"));
        JSONObject plan=sync.optJSONObject("plan");if(plan!=null){LinearLayout c=card();c.setBackground(gradient(NAVY,NAVY2,20));c.addView(text(plan.optString("month_key").toUpperCase()+" PLAN",10,Color.rgb(148,163,184),true));c.addView(text(plan.optInt("lesson_target")+" lessons planned",26,Color.WHITE,true));c.addView(text(plan.optString("goal"),12,Color.rgb(203,213,225),false));body.addView(c,marginBottom(14));}
        section("Next lesson");JSONObject next=nextStudentLesson();if(next==null)body.addView(empty("No upcoming lesson yet."));else body.addView(studentLessonCard(next),marginBottom(8));
        section("Announcements");JSONArray a=sync.optJSONArray("announcements");for(int i=0;i<a.length();i++){JSONObject x=a.optJSONObject(i);LinearLayout c=card();c.addView(text(x.optString("title"),15,NAVY,true));c.addView(text(x.optString("body"),12,MUTED,false));body.addView(c,marginBottom(8));}
    }

    private void studentLearning(){
        JSONObject u=sync.optJSONObject("user");
        pageTitle("My learning","Your syllabus, progress and resources in one place.");

        JSONArray topics=sync.optJSONArray("topicProgress");
        int covered=0,next=0,revisit=0,total=topics.length();
        for(int i=0;i<topics.length();i++){String st=topics.optJSONObject(i).optString("status");if("Covered".equals(st)||"Completed".equals(st))covered++;else if("Next".equals(st)||"Current".equals(st))next++;else if("Revisit".equals(st))revisit++;}
        LinearLayout summary=card();summary.setBackground(gradient(NAVY,Color.rgb(31,55,96),20));
        summary.addView(text(nz(u.optString("grade"))+" · "+nz(u.optString("subjects")),12,Color.rgb(203,213,225),true));
        summary.addView(text(covered+" of "+total+" topics covered",25,Color.WHITE,true),marginTopBottom(7,4));
        summary.addView(text(next+" current/next  •  "+revisit+" to revisit",11,Color.rgb(203,213,225),false));
        body.addView(summary,marginBottom(14));

        String lastGroup="";
        for(int i=0;i<topics.length();i++){
            JSONObject x=topics.optJSONObject(i);
            String group=x.optString("term_label")+" · "+x.optString("strand");
            if(!group.equals(lastGroup)){section(group);lastGroup=group;}
            LinearLayout c=card();LinearLayout top=row();top.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout info=col();info.addView(text(x.optString("topic"),14,NAVY,true));if(!x.optString("notes").isEmpty())info.addView(text(x.optString("notes"),10,MUTED,false),marginTopBottom(4,0));top.addView(info,new LinearLayout.LayoutParams(0,-2,1));
            top.addView(pill(x.optString("status"),statusBg(x.optString("status")),statusColor(x.optString("status"))));c.addView(top);
            body.addView(c,marginBottom(7));
        }

        section("Resources");
        JSONArray rs=sync.optJSONArray("resources");if(rs.length()==0)body.addView(empty("No resources yet."));
        for(int i=0;i<rs.length();i++){JSONObject r=rs.optJSONObject(i);if(!r.optBoolean("featured")&&i>4)continue;body.addView(studentResourceCard(r),marginBottom(8));}
    }

    private void studentPlan(){
        JSONObject p=sync.optJSONObject("plan");pageTitle("My learning plan",p==null?"Victoria hasn’t set a plan yet.":p.optString("month_key"));
        if(p==null)return;LinearLayout c=card();c.addView(text(p.optInt("lesson_target")+" lessons",28,ORANGE,true));c.addView(text(p.optString("goal"),13,TEXT,false));body.addView(c,marginBottom(12));
        section("Focus topics");JSONArray t=p.optJSONArray("focus_topics");if(t!=null)for(int i=0;i<t.length();i++){LinearLayout x=card();x.addView(text((i+1)+". "+t.optString(i),15,NAVY,true));body.addView(x,marginBottom(7));}
    }

    private void studentLessons(){
        pageTitle("My lessons","What you’ve covered, what’s planned, and your booking requests.");
        JSONArray bs=sync.optJSONArray("bookings");if(bs.length()>0){section("Requests");for(int i=0;i<bs.length();i++){JSONObject b=bs.optJSONObject(i);LinearLayout c=card();c.addView(pill(b.optString("status"),ORANGE_SOFT,ORANGE));c.addView(text(b.optString("requested_date")+" · "+shortTime(b.optString("requested_time")),15,NAVY,true),marginTopBottom(7,2));c.addView(text(b.optString("subject")+" · "+b.optString("topic"),12,MUTED,false));body.addView(c,marginBottom(8));}}
        section("Lesson history");JSONArray ls=sync.optJSONArray("lessons");if(ls.length()==0)body.addView(empty("No lessons yet."));for(int i=0;i<ls.length();i++)body.addView(studentLessonCard(ls.optJSONObject(i)),marginBottom(8));
    }

    private void studentBook(){
        pageTitle("Book a lesson","Start with a suggested topic, then choose a date and live available time.");

        LinearLayout intro=card();
        intro.addView(text("Not sure what to cover?",16,NAVY,true));
        intro.addView(text("Choose one of the recommended topics below. They are based on your current subject and progress.",12,MUTED,false));
        body.addView(intro,marginBottom(14));

        section("Recommended for you");
        String[] suggestions=recommendedBookingTopics();
        for(String topic:suggestions){
            LinearLayout option=card();
            option.addView(text(topic,15,NAVY,true));
            option.addView(text("Tap to book this topic",11,MUTED,false));
            option.setOnClickListener(v->dialogBooking(topic));
            body.addView(option,marginBottom(8));
        }

        Button other=secondary("Choose another topic");
        other.setOnClickListener(v->dialogBooking(""));
        body.addView(other,marginBottom(12));

        TextView p=text("Your booking calendar only shows Available / Unavailable. Other students’ names and lesson details stay private.",12,MUTED,false);
        body.addView(p);
    }

    private String[] recommendedBookingTopics(){
        String name=sync.optJSONObject("user").optString("displayName").toLowerCase(Locale.ROOT);
        if(name.contains("mitchell")) return new String[]{
            "Electric Cells & Batteries",
            "Current, Voltage & Resistance",
            "Balanced & Unbalanced Forces",
            "Gravity, Mass & Weight",
            "Magnetism & Electrostatics"
        };
        if(name.contains("lateya")) return new String[]{
            "Functions & Graphs",
            "Parabola, Hyperbola & Exponential",
            "Algebraic Expressions",
            "Trigonometric Graphs",
            "Exam-style Functions Practice"
        };
        if(name.contains("phelandi")) return new String[]{
            "Equations of Motion",
            "Factors Affecting Resistance",
            "Electric Cells",
            "Mathematics Term 1–3 Revision"
        };
        return new String[]{
            "Speed & Velocity",
            "Acceleration",
            "1D Motion & Motion Graphs",
            "Equations of Motion",
            "Physics Exam Practice"
        };
    }

    private void studentResources(){
        pageTitle("My resources","Choose a category, then open what you need.");
        JSONArray rs=sync.optJSONArray("resources");if(rs.length()==0){body.addView(empty("No resources yet."));return;}
        String[] types={"Interactive Lesson","Slides","PDF / Notes","Worksheet","Video","Image","Other"};
        String[] labels={"Interactive lessons","Slides","Notes & PDFs","Worksheets","Videos","Images","Other"};
        for(int i=0;i<types.length;i++){
            String type=types[i];int count=0;for(int j=0;j<rs.length();j++)if(type.equals(rs.optJSONObject(j).optString("resource_type")))count++;
            if(count==0)continue;
            LinearLayout c=card();LinearLayout top=row();top.setGravity(Gravity.CENTER_VERTICAL);
            LinearLayout info=col();info.addView(text(labels[i],16,NAVY,true));info.addView(text(count+" item"+(count==1?"":"s"),11,MUTED,false));top.addView(info,new LinearLayout.LayoutParams(0,-2,1));top.addView(text("›",26,NAVY,false));c.addView(top);
            c.setOnClickListener(v->studentResourceCategory(type));body.addView(c,marginBottom(9));
        }
    }

    private void studentResourceCategory(String type){
        body.removeAllViews();TextView back=text("‹  My resources",14,NAVY,true);back.setPadding(0,0,0,dp(14));back.setOnClickListener(v->studentResources());body.addView(back);
        pageTitle(type,"Resources shared with you by Victoria.");
        JSONArray rs=sync.optJSONArray("resources");int count=0;
        for(int i=0;i<rs.length();i++){JSONObject r=rs.optJSONObject(i);if(!type.equals(r.optString("resource_type")))continue;count++;body.addView(studentResourceCard(r),marginBottom(8));}
        if(count==0)body.addView(empty("Nothing in this category yet."));
    }

    private LinearLayout studentResourceCard(JSONObject r){
        LinearLayout c=card();LinearLayout top=row();top.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout info=col();info.addView(text(r.optString("title"),15,NAVY,true));info.addView(text(r.optString("completion_status"),10,statusColor(r.optString("completion_status")),true));top.addView(info,new LinearLayout.LayoutParams(0,-2,1));
        top.addView(pill(r.optString("resource_type"),Color.rgb(245,243,255),PURPLE));c.addView(top);
        if(!r.optString("description").isEmpty())c.addView(text(r.optString("description"),11,MUTED,false),marginTopBottom(7,0));
        if(!r.optString("access_note").isEmpty())c.addView(text(r.optString("access_note"),11,ORANGE,true),marginTopBottom(7,0));
        Button open=smallButton("Open resource");open.setOnClickListener(v->downloadResource(r));c.addView(open,marginTopBottom(10,0));return c;
    }

    private void studentMessages(){
        pageTitle("Victoria","Tutor chat");
        JSONArray ms=sync.optJSONArray("messages");String me=sync.optJSONObject("user").optString("id");
        if(ms.length()==0)body.addView(empty("No messages yet. Send a message below to start the conversation."));
        for(int i=ms.length()-1;i>=0;i--){JSONObject m=ms.optJSONObject(i);body.addView(chatBubble(m,me.equals(m.optString("sender_id"))),marginBottom(6));}

        LinearLayout composer=row();composer.setGravity(Gravity.CENTER_VERTICAL);composer.setPadding(dp(6),dp(6),dp(6),dp(6));composer.setBackground(round(Color.WHITE,22,1,LINE));
        EditText msg=input("Message Victoria",false);msg.setSingleLine(false);msg.setMaxLines(4);composer.addView(msg,new LinearLayout.LayoutParams(0,-2,1));
        TextView send=text("➤",19,Color.WHITE,true);send.setGravity(Gravity.CENTER);send.setBackground(circle(Color.rgb(0,168,132)));composer.addView(send,new LinearLayout.LayoutParams(dp(44),dp(44)));
        send.setOnClickListener(v->{String value=msg.getText().toString().trim();if(value.isEmpty())return;msg.setText("");JSONObject q=new JSONObject();try{q.put("action","messageSend");q.put("message",value);}catch(Exception ignored){}action(q,()->openTab("Messages"));});
        body.addView(composer,marginTopBottom(12,0));
    }

    private LinearLayout chatBubble(JSONObject m,boolean mine){
        LinearLayout wrap=row();wrap.setGravity(mine?Gravity.RIGHT:Gravity.LEFT);
        LinearLayout bubble=col();bubble.setPadding(dp(12),dp(8),dp(10),dp(6));
        bubble.setBackground(round(mine?Color.rgb(217,253,211):Color.WHITE,16,1,mine?Color.rgb(187,247,208):LINE));
        bubble.addView(text(m.optString("body"),14,TEXT,false));
        String stamp=m.optString("created_at");if(stamp.length()>=16)stamp=stamp.substring(11,16);
        TextView time=text(stamp,9,MUTED,false);time.setGravity(Gravity.RIGHT);bubble.addView(time,marginTopBottom(4,0));
        int width=(int)(getResources().getDisplayMetrics().widthPixels*0.74f);
        wrap.addView(bubble,new LinearLayout.LayoutParams(width,-2));return wrap;
    }

    // ---------- BOOKING ----------

    private void dialogBooking(){dialogBooking("");}

    private void dialogBooking(String suggestedTopic){
        LinearLayout box=col();box.setPadding(dp(4),dp(4),dp(4),0);
        final String[] date={""},time={""};final int[] dur={60};
        Button dateBtn=secondary("1. Choose date");box.addView(dateBtn,marginBottom(8));
        Spinner duration=new Spinner(this);duration.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"1 hour","2 hours"}));box.addView(duration,marginBottom(8));
        Spinner subject=new Spinner(this);subject.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Mathematics","Physics","Natural Sciences"}));box.addView(subject,marginBottom(8));
        EditText topic=input("Topic",false),help=input("What exactly do you need help with?",false);topic.setText(suggestedTopic);box.addView(topic,marginBottom(8));box.addView(help,marginBottom(8));
        TextView label=text("Available times",12,MUTED,true);box.addView(label);
        GridLayout grid=new GridLayout(this);grid.setColumnCount(3);box.addView(grid);

        Runnable load=()->{
            grid.removeAllViews();if(date[0].isEmpty()){grid.addView(text("Choose a date first.",12,MUTED,false));return;}
            JSONObject p=new JSONObject();try{p.put("action","availability");p.put("date",date[0]);p.put("duration",dur[0]);}catch(Exception ignored){}
            grid.addView(text("Checking live availability…",12,MUTED,false));
            api(p,true,(r,e)->{
                grid.removeAllViews();if(e!=null){grid.addView(text(e.getMessage(),12,Color.RED,false));return;}
                JSONArray slots=r.optJSONArray("slots");for(int i=0;i<slots.length();i++){JSONObject s=slots.optJSONObject(i);boolean free=s.optBoolean("available");Button x=new Button(this);x.setAllCaps(false);x.setText(s.optString("time")+"\n"+(free?"Available":"Unavailable"));x.setTextSize(10);x.setEnabled(free);x.setTextColor(free?NAVY:MUTED);x.setBackground(round(free?Color.WHITE:Color.rgb(241,245,249),12,1,LINE));GridLayout.LayoutParams lp=new GridLayout.LayoutParams();lp.width=0;lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);lp.setMargins(dp(3),dp(3),dp(3),dp(3));x.setLayoutParams(lp);
                    if(free)x.setOnClickListener(v->{time[0]=s.optString("time");for(int j=0;j<grid.getChildCount();j++){View y=grid.getChildAt(j);if(y instanceof Button && y.isEnabled())y.setBackground(round(Color.WHITE,12,1,LINE));}x.setBackground(round(ORANGE_SOFT,12,2,ORANGE));});grid.addView(x);}
            });
        };
        dateBtn.setOnClickListener(v->pickDate(dateBtn,date,load));
        duration.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?> p,View v,int pos,long id){dur[0]=pos==0?60:120;time[0]="";load.run();}public void onNothingSelected(AdapterView<?> p){}});
        AlertDialog d=new AlertDialog.Builder(this).setTitle("Request a lesson").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Send request",null).create();
        d.setOnShowListener(x->d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v->{
            if(date[0].isEmpty()||time[0].isEmpty()){toast("Choose a date and available time.");return;}
            JSONObject p=new JSONObject();try{p.put("action","bookingCreate");p.put("date",date[0]);p.put("time",time[0]);p.put("duration",dur[0]);p.put("subject",subject.getSelectedItem().toString());p.put("topic",topic.getText().toString());p.put("help",help.getText().toString());}catch(Exception ignored){}
            action(p,()->{d.dismiss();toast("Booking request sent.");openTab("Lessons");});
        }));d.show();
    }

    // ---------- ADMIN ACTION DIALOGS ----------

    private void dialogCreateStudent(){
        LinearLayout box=formBox();EditText n=input("Student name",false),g=input("Grade",false),c=input("Curriculum",false),s=input("Subjects",false),u=input("Username",false),p=input("Temporary password",true);
        for(EditText e:new EditText[]{n,g,c,s,u,p})box.addView(e,marginBottom(8));
        new AlertDialog.Builder(this).setTitle("Create student portal").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Create",(d,w)->{JSONObject q=new JSONObject();try{q.put("action","studentCreate");q.put("name",n.getText().toString());q.put("grade",g.getText().toString());q.put("curriculum",c.getText().toString());q.put("subjects",s.getText().toString());q.put("username",u.getText().toString());q.put("password",p.getText().toString());}catch(Exception ignored){}action(q,()->{toast("Student account created.");openTab("Students");});}).show();
    }

    private void dialogPlan(JSONObject student,JSONObject old){
        LinearLayout box=formBox();EditText month=input("Month key, e.g. 2026-10",false),target=input("Lessons this month",false),goal=input("Monthly goal",false),topics=input("Focus topics separated by commas",false);
        target.setInputType(InputType.TYPE_CLASS_NUMBER);if(old!=null){month.setText(old.optString("month_key"));target.setText(String.valueOf(old.optInt("lesson_target")));goal.setText(old.optString("goal"));JSONArray a=old.optJSONArray("focus_topics");List<String> t=new ArrayList<>();if(a!=null)for(int i=0;i<a.length();i++)t.add(a.optString(i));topics.setText(android.text.TextUtils.join(", ",t));}
        for(EditText e:new EditText[]{month,target,goal,topics})box.addView(e,marginBottom(8));
        new AlertDialog.Builder(this).setTitle("Edit "+student.optString("display_name")+" plan").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{JSONObject q=new JSONObject();try{q.put("action","planUpdate");q.put("studentId",student.optString("id"));q.put("monthKey",month.getText().toString());q.put("lessonTarget",Integer.parseInt(target.getText().toString().trim().isEmpty()?"0":target.getText().toString().trim()));q.put("goal",goal.getText().toString());JSONArray a=new JSONArray();for(String x:topics.getText().toString().split(","))if(!x.trim().isEmpty())a.put(x.trim());q.put("focusTopics",a);}catch(Exception ignored){}action(q,()->openTab("Students"));}).show();
    }

    private void dialogAddLesson(){
        JSONArray st=sync.optJSONArray("students");String[] names=new String[st.length()];for(int i=0;i<st.length();i++)names[i]=st.optJSONObject(i).optString("display_name");
        LinearLayout box=formBox();Spinner sp=new Spinner(this);sp.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,names));final String[] date={""},time={""};Button db=secondary("Choose date"),tb=secondary("Choose time");db.setOnClickListener(v->pickDate(db,date,()->{}));tb.setOnClickListener(v->pickTime(tb,time));Spinner dur=new Spinner(this);dur.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"60 minutes","120 minutes"}));EditText sub=input("Subject",false),topic=input("Topic",false),status=input("Status: Planned / Confirmed / Completed",false),notes=input("Notes",false);status.setText("Planned");
        box.addView(sp,marginBottom(8));box.addView(db,marginBottom(8));box.addView(tb,marginBottom(8));box.addView(dur,marginBottom(8));for(EditText e:new EditText[]{sub,topic,status,notes})box.addView(e,marginBottom(8));
        new AlertDialog.Builder(this).setTitle("Add / log lesson").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{if(date[0].isEmpty()){toast("Choose a date.");return;}JSONObject student=st.optJSONObject(sp.getSelectedItemPosition());JSONObject q=new JSONObject();try{q.put("action","lessonUpsert");q.put("studentId",student.optString("id"));q.put("date",date[0]);q.put("time",time[0]);q.put("duration",dur.getSelectedItemPosition()==0?60:120);q.put("subject",sub.getText().toString());q.put("topic",topic.getText().toString());q.put("status",status.getText().toString());q.put("notes",notes.getText().toString());}catch(Exception ignored){}action(q,()->openTab("Schedule"));}).show();
    }

    private void dialogScheduleBlock(){
        LinearLayout box=formBox();final String[] date={""},start={""},end={""};Button d=secondary("Choose date"),s=secondary("Start time"),e=secondary("End time");d.setOnClickListener(v->pickDate(d,date,()->{}));s.setOnClickListener(v->pickTime(s,start));e.setOnClickListener(v->pickTime(e,end));EditText title=input("Private reason / title",false);box.addView(d,marginBottom(8));box.addView(s,marginBottom(8));box.addView(e,marginBottom(8));box.addView(title);
        new AlertDialog.Builder(this).setTitle("Add private schedule block").setMessage("Students will only see the time as unavailable.").setView(box).setNegativeButton("Cancel",null).setPositiveButton("Save",(x,w)->{JSONObject q=new JSONObject();try{q.put("action","scheduleCreate");q.put("date",date[0]);q.put("start",start[0]);q.put("end",end[0]);q.put("title",title.getText().toString());q.put("visibility","PRIVATE");}catch(Exception ignored){}action(q,()->openTab("Schedule"));}).show();
    }

    private LinearLayout bookingRequestCard(JSONObject b,String name){
        LinearLayout c=card();c.addView(text(name,15,NAVY,true));c.addView(text(b.optString("requested_date")+" · "+shortTime(b.optString("requested_time"))+" · "+b.optInt("duration_minutes")+" min",13,TEXT,true),marginTopBottom(7,2));c.addView(text(b.optString("subject")+" · "+b.optString("topic"),12,MUTED,false));LinearLayout a=row();Button yes=smallButton("Confirm"),no=smallButton("Decline");yes.setOnClickListener(v->bookingStatus(b,"Confirmed"));no.setOnClickListener(v->bookingStatus(b,"Declined"));a.addView(yes,new LinearLayout.LayoutParams(0,dp(44),1));a.addView(no,weightMarginHeight(1,8,44));c.addView(a,marginTopBottom(10,0));return c;
    }

    private void bookingStatus(JSONObject b,String status){JSONObject q=new JSONObject();try{q.put("action","bookingStatus");q.put("id",b.optString("id"));q.put("status",status);}catch(Exception ignored){}action(q,()->openTab("Schedule"));}

    private void dialogMessage(String studentId){
        EditText m=input("Write message",false);new AlertDialog.Builder(this).setTitle("Message").setView(m).setNegativeButton("Cancel",null).setPositiveButton("Send",(d,w)->{JSONObject q=new JSONObject();try{q.put("action","messageSend");if(studentId!=null)q.put("studentId",studentId);q.put("message",m.getText().toString());}catch(Exception ignored){}action(q,()->openTab("Messages"));}).show();
    }

    private void dialogAnnouncement(){
        LinearLayout b=formBox();EditText t=input("Title",false),m=input("Message",false);b.addView(t,marginBottom(8));b.addView(m);
        new AlertDialog.Builder(this).setTitle("Post announcement").setView(b).setNegativeButton("Cancel",null).setPositiveButton("Post",(d,w)->{JSONObject q=new JSONObject();try{q.put("action","announcementCreate");q.put("title",t.getText().toString());q.put("message",m.getText().toString());q.put("audience","ALL");}catch(Exception ignored){}action(q,()->openTab("Messages"));}).show();
    }

    private void dialogPayment(){
        LinearLayout b=formBox();EditText p=input("Payer",false),d=input("Date YYYY-MM-DD",false),a=input("Amount in rand",false),n=input("Note",false);a.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);for(EditText e:new EditText[]{p,d,a,n})b.addView(e,marginBottom(8));
        new AlertDialog.Builder(this).setTitle("Record payment").setView(b).setNegativeButton("Cancel",null).setPositiveButton("Save",(x,w)->{JSONObject q=new JSONObject();try{q.put("action","paymentCreate");q.put("payer",p.getText().toString());q.put("date",d.getText().toString());if(!a.getText().toString().trim().isEmpty())q.put("amountCents",Math.round(Double.parseDouble(a.getText().toString())*100));q.put("note",n.getText().toString());}catch(Exception ignored){}action(q,()->openTab("Finance"));}).show();
    }

    private void dialogInvoice(){
        LinearLayout b=formBox();EditText number=input("Invoice number",false),payer=input("Payer",false),start=input("Period start YYYY-MM-DD",false),end=input("Period end YYYY-MM-DD",false);payer.setText("Tracey");for(EditText e:new EditText[]{number,payer,start,end})b.addView(e,marginBottom(8));
        new AlertDialog.Builder(this).setTitle("Create invoice draft").setView(b).setNegativeButton("Cancel",null).setPositiveButton("Create",(d,w)->{JSONObject q=new JSONObject();try{q.put("action","invoiceCreate");q.put("invoiceNumber",number.getText().toString());q.put("payer",payer.getText().toString());q.put("periodStart",start.getText().toString());q.put("periodEnd",end.getText().toString());q.put("hourlyRateCents",15000);}catch(Exception ignored){}action(q,()->openTab("Finance"));}).show();
    }

    private void settingCard(String title,String value,String[] options,String key){
        LinearLayout c=card();c.addView(text(title,15,NAVY,true));c.addView(text(value,12,MUTED,false));Button b=smallButton("Change");b.setOnClickListener(v->new AlertDialog.Builder(this).setTitle(title).setItems(options,(d,which)->{JSONObject q=new JSONObject();try{q.put("action","settingUpdate");q.put("key",key);q.put("value",options[which]);}catch(Exception ignored){}action(q,()->openTab("Format"));}).show());c.addView(b,marginTopBottom(9,0));body.addView(c,marginBottom(9));
    }

    // ---------- RESOURCE UPLOAD/DOWNLOAD ----------

    private void chooseStudentForResource(){
        JSONArray s=sync.optJSONArray("students");String[] names=new String[s.length()];for(int i=0;i<s.length();i++)names[i]=s.optJSONObject(i).optString("display_name");
        new AlertDialog.Builder(this).setTitle("Choose student").setItems(names,(d,which)->resourceMeta(s.optJSONObject(which))).show();
    }

    private void resourceMeta(JSONObject student){
        LinearLayout b=formBox();
        EditText title=input("Resource title",false),desc=input("Short description",false),access=input("Access note / lesson password (optional)",false);
        Spinner type=new Spinner(this);type.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Interactive Lesson","Slides","PDF / Notes","Worksheet","Video","Image","Other"}));
        b.addView(title,marginBottom(8));b.addView(type,marginBottom(8));b.addView(desc,marginBottom(8));b.addView(access);
        new AlertDialog.Builder(this).setTitle("Upload for "+student.optString("display_name")).setView(b).setNegativeButton("Cancel",null).setPositiveButton("Choose file",(d,w)->{
            pendingStudent=student;pendingResourceTitle=title.getText().toString();pendingResourceType=type.getSelectedItem().toString();pendingResourceDescription=desc.getText().toString();pendingResourceAccessNote=access.getText().toString();
            Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");startActivityForResult(i,FILE_PICK);
        }).show();
    }

    @Override protected void onActivityResult(int requestCode,int resultCode,Intent intent){
        super.onActivityResult(requestCode,resultCode,intent);
        if(requestCode!=FILE_PICK||resultCode!=RESULT_OK||intent==null||intent.getData()==null||pendingStudent==null)return;
        Uri uri=intent.getData();
        try{
            byte[] bytes=readBytes(uri,8*1024*1024+1);if(bytes.length>8*1024*1024){toast("File is over 8 MB in this build.");return;}
            String name=fileName(uri),mime=getContentResolver().getType(uri);if(mime==null)mime="application/octet-stream";
            JSONObject q=new JSONObject();q.put("action","resourceUpload");q.put("studentId",pendingStudent.optString("id"));q.put("title",pendingResourceTitle.isEmpty()?name:pendingResourceTitle);q.put("resourceType",pendingResourceType);q.put("fileName",name);q.put("mimeType",mime);q.put("description",pendingResourceDescription);q.put("accessNote",pendingResourceAccessNote);q.put("base64",Base64.encodeToString(bytes,Base64.NO_WRAP));
            toast("Uploading…");action(q,()->{toast("Resource uploaded.");openTab("Resources");});
        }catch(Exception e){toast("Could not upload: "+e.getMessage());}
        pendingStudent=null;
    }

    private byte[] readBytes(Uri uri,int max) throws Exception{InputStream in=getContentResolver().openInputStream(uri);ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n,total=0;while((n=in.read(buf))!=-1){total+=n;if(total>max)break;out.write(buf,0,n);}in.close();return out.toByteArray();}
    private String fileName(Uri uri){String n="file";android.database.Cursor c=null;try{c=getContentResolver().query(uri,null,null,null,null);if(c!=null&&c.moveToFirst()){int i=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(i>=0)n=c.getString(i);}}finally{if(c!=null)c.close();}return n;}

    private void downloadResource(JSONObject r){
        LinearLayout b=formBox();b.addView(pill(r.optString("resource_type"),Color.rgb(245,243,255),PURPLE));b.addView(text(r.optString("title"),18,NAVY,true),marginTopBottom(10,3));b.addView(text(r.optString("file_name"),12,MUTED,false));
        new AlertDialog.Builder(this).setTitle("Resource preview").setView(b).setNegativeButton("Close",null).setPositiveButton("Open file",(d,w)->{
            String external=r.optString("external_url");
            if(!external.isEmpty()){try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(external)));}catch(Exception x){toast("No app available to open this resource.");}return;}
            JSONObject q=new JSONObject();try{q.put("action","resourceDownload");q.put("id",r.optString("id"));}catch(Exception ignored){}
            api(q,true,(res,e)->{if(e!=null){toast(e.getMessage());return;}try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(res.optString("signedUrl"))));}catch(Exception x){toast("No app available to open this file.");}});
        }).show();
    }

    // ---------- NOTIFICATIONS ----------

    private void createNotificationChannel(){if(Build.VERSION.SDK_INT>=26){NotificationChannel c=new NotificationChannel("vta_updates","VTA Updates",NotificationManager.IMPORTANCE_DEFAULT);((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).createNotificationChannel(c);}}
    private void showNewNotifications(){
        JSONArray n=sync.optJSONArray("notifications");if(n==null)return;String seen=prefs.getString("lastNotification","");
        for(int i=0;i<n.length();i++){JSONObject x=n.optJSONObject(i);String id=x.optString("id");if(id.equals(seen))break;notifyLocal(x.optString("title"),x.optString("body"));}
        if(n.length()>0)prefs.edit().putString("lastNotification",n.optJSONObject(0).optString("id")).apply();
    }
    private void notifyLocal(String title,String msg){Notification.Builder b=Build.VERSION.SDK_INT>=26?new Notification.Builder(this,"vta_updates"):new Notification.Builder(this);b.setSmallIcon(android.R.drawable.ic_dialog_info).setContentTitle(title).setContentText(msg).setAutoCancel(true);((NotificationManager)getSystemService(NOTIFICATION_SERVICE)).notify((int)(System.currentTimeMillis()%100000),b.build());}

    // ---------- HELPERS ----------

    private JSONObject settingsMap(){JSONObject o=new JSONObject();JSONArray a=sync.optJSONArray("settings");try{for(int i=0;i<a.length();i++){JSONObject x=a.optJSONObject(i);Object v=x.opt("setting_value");o.put(x.optString("setting_key"),v);}}catch(Exception ignored){}return o;}
    private JSONObject findBy(JSONArray a,String key,String value){if(a==null)return null;for(int i=0;i<a.length();i++){JSONObject x=a.optJSONObject(i);if(x!=null&&value.equals(x.optString(key)))return x;}return null;}
    private String studentName(String id){JSONObject s=findBy(sync.optJSONArray("students"),"id",id);return s==null?"Student":s.optString("display_name");}
    private int countWhere(JSONArray a,String key,String val){int n=0;if(a!=null)for(int i=0;i<a.length();i++)if(val.equals(a.optJSONObject(i).optString(key)))n++;return n;}
    private int upcomingLessons(){int n=0;JSONArray a=sync.optJSONArray("lessons");if(a!=null)for(int i=0;i<a.length();i++)if(!"Completed".equals(a.optJSONObject(i).optString("status")))n++;return n;}
    private int invoiceReadyTotal(){int c=0;JSONArray a=sync.optJSONArray("lessons");if(a!=null)for(int i=0;i<a.length();i++){JSONObject l=a.optJSONObject(i);if("Completed".equals(l.optString("status")))c+=l.optInt("duration_minutes")*150/60;}return c;}
    private JSONObject nextStudentLesson(){JSONArray a=sync.optJSONArray("lessons");if(a==null)return null;JSONObject best=null;String bk="";for(int i=0;i<a.length();i++){JSONObject l=a.optJSONObject(i);if("Completed".equals(l.optString("status"))||"Cancelled".equals(l.optString("status")))continue;String k=l.optString("lesson_date")+l.optString("start_time");if(best==null||k.compareTo(bk)<0){best=l;bk=k;}}return best;}

    private LinearLayout adminLessonCard(JSONObject l){LinearLayout c=card();c.addView(text(studentName(l.optString("student_id")),14,NAVY,true));c.addView(text(l.optString("topic"),16,TEXT,true),marginTopBottom(6,2));c.addView(text(l.optString("lesson_date")+" · "+shortTime(l.optString("start_time"))+" · "+l.optInt("duration_minutes")+" min",11,MUTED,false));c.addView(pill(l.optString("status"),statusBg(l.optString("status")),statusColor(l.optString("status"))),marginTopBottom(7,0));return c;}
    private LinearLayout studentLessonCard(JSONObject l){LinearLayout c=card();c.addView(pill(l.optString("status"),statusBg(l.optString("status")),statusColor(l.optString("status"))));c.addView(text(l.optString("topic"),16,NAVY,true),marginTopBottom(8,2));String t=l.optString("lesson_date")+" · "+(l.optString("start_time").isEmpty()?"Time not recorded":shortTime(l.optString("start_time")))+" · "+l.optInt("duration_minutes")+" min";c.addView(text(t,11,MUTED,false));c.addView(text(l.optString("subject"),11,ORANGE,true));if(!l.optString("notes").isEmpty())c.addView(text(l.optString("notes"),12,TEXT,false),marginTopBottom(7,0));return c;}

    private void pickDate(Button b,String[] holder,Runnable after){Calendar n=Calendar.getInstance();new DatePickerDialog(this,(v,y,m,d)->{holder[0]=String.format(Locale.ENGLISH,"%04d-%02d-%02d",y,m+1,d);b.setText(holder[0]);after.run();},n.get(Calendar.YEAR),n.get(Calendar.MONTH),n.get(Calendar.DAY_OF_MONTH)).show();}
    private void pickTime(Button b,String[] holder){Calendar n=Calendar.getInstance();new TimePickerDialog(this,(v,h,m)->{holder[0]=String.format(Locale.ENGLISH,"%02d:%02d",h,m);b.setText(holder[0]);},n.get(Calendar.HOUR_OF_DAY),0,true).show();}

    private void pageTitle(String t,String s){body.addView(text(t,29,NAVY,true));TextView x=text(s,13,MUTED,false);x.setPadding(0,dp(4),0,dp(15));body.addView(x);}
    private void section(String t){TextView x=text(t,16,NAVY,true);x.setPadding(0,dp(18),0,dp(9));body.addView(x);}
    private int themeColor(int c){
        if(!darkMode)return c;
        if(c==BG)return Color.rgb(9,15,26);
        if(c==Color.WHITE)return Color.rgb(19,29,46);
        if(c==NAVY||c==TEXT)return Color.rgb(241,245,249);
        if(c==MUTED)return Color.rgb(148,163,184);
        if(c==LINE||c==Color.rgb(203,213,225))return Color.rgb(51,65,85);
        if(c==Color.rgb(248,250,252))return Color.rgb(28,39,58);
        return c;
    }
    private LinearLayout card(){LinearLayout c=col();c.setPadding(dp(16),dp(16),dp(16),dp(16));c.setBackground(round(Color.WHITE,18,1,LINE));return c;}
    private LinearLayout empty(String s){LinearLayout c=card();c.addView(text(s,13,MUTED,false));return c;}
    private LinearLayout metric(String l,String v,int color){LinearLayout c=card();c.addView(text(v,22,color,true));c.addView(text(l,10,MUTED,true));return c;}
    private LinearLayout quick(String icon,String title,String sub,int accent,View.OnClickListener l){LinearLayout c=card();c.addView(pill(icon,light(accent),accent));c.addView(text(title,14,NAVY,true),marginTopBottom(9,2));c.addView(text(sub,11,MUTED,false));c.setOnClickListener(l);return c;}
    private TextView pill(String s,int bg,int fg){TextView t=text(s,10,fg,true);t.setGravity(Gravity.CENTER);t.setPadding(dp(10),dp(6),dp(10),dp(6));t.setBackground(round(bg,100,0,0));return t;}
    private Button primary(String s){return button(s,ORANGE,Color.WHITE);}
    private Button secondary(String s){Button b=button(s,Color.WHITE,NAVY);b.setBackground(round(Color.WHITE,14,1,LINE));return b;}
    private Button smallButton(String s){Button b=button(s,Color.rgb(248,250,252),NAVY);b.setTextSize(11);b.setBackground(round(Color.rgb(248,250,252),12,1,LINE));return b;}
    private EditText input(String hint,boolean pass){EditText e=new EditText(this);e.setHint(hint);e.setTextColor(themeColor(NAVY));e.setHintTextColor(themeColor(MUTED));e.setTextSize(14);e.setPadding(dp(13),dp(11),dp(13),dp(11));e.setBackground(round(Color.WHITE,13,1,Color.rgb(203,213,225)));if(pass)e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);return e;}
    private Button button(String s,int bg,int fg){Button b=new Button(this);b.setText(s);b.setTextColor(themeColor(fg));b.setTextSize(13);b.setAllCaps(false);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setBackground(round(bg,14,0,0));return b;}
    private TextView text(String s,float z,int c,boolean bold){TextView t=new TextView(this);t.setText(s);t.setTextSize(z);t.setTextColor(themeColor(c));if(bold)t.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return t;}
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);return l;}
    private LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private LinearLayout formBox(){LinearLayout b=col();b.setPadding(dp(3),dp(8),dp(3),0);return b;}
    private GradientDrawable round(int c,int r,int stroke,int sc){GradientDrawable g=new GradientDrawable();g.setColor(themeColor(c));g.setCornerRadius(dp(r));if(stroke>0)g.setStroke(dp(stroke),themeColor(sc));return g;}
    private GradientDrawable gradient(int a,int b,int r){GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a,b});g.setCornerRadius(dp(r));return g;}
    private GradientDrawable circle(int c){GradientDrawable g=new GradientDrawable();g.setShape(GradientDrawable.OVAL);g.setColor(c);return g;}
    private LinearLayout.LayoutParams marginBottom(int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,dp(b));return p;}
    private LinearLayout.LayoutParams marginTopBottom(int t,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(t),0,dp(b));return p;}
    private LinearLayout.LayoutParams marginRight(int r){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,-2);p.setMargins(0,0,dp(r),0);return p;}
    private LinearLayout.LayoutParams marginLeft(int l){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,-2);p.setMargins(dp(l),0,0,0);return p;}
    private LinearLayout.LayoutParams weightMargin(int w,int l){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,w);p.setMargins(dp(l),0,0,0);return p;}
    private LinearLayout.LayoutParams weightMarginHeight(int w,int l,int h){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(h),w);p.setMargins(dp(l),0,0,0);return p;}
    private int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    private int colorFor(int i){int[] c={ORANGE,BLUE,PURPLE,GREEN,Color.rgb(234,88,12)};return c[i%c.length];}
    private int light(int c){if(c==ORANGE)return ORANGE_SOFT;if(c==GREEN)return GREEN_SOFT;if(c==BLUE)return Color.rgb(239,246,255);return Color.rgb(245,243,255);}
    private int statusColor(String s){
        if("Covered".equals(s)||"Completed".equals(s)||"Available".equals(s))return Color.rgb(21,128,61);
        if("Current".equals(s)||"Confirmed".equals(s)||"Scheduled".equals(s))return BLUE;
        if("Next".equals(s))return ORANGE;
        if("Revisit".equals(s))return Color.rgb(217,119,6);
        if("Resource Ready".equals(s))return PURPLE;
        if("Cancelled".equals(s)||"Declined".equals(s))return Color.rgb(185,28,28);
        return MUTED;
    }
    private int statusBg(String s){
        if("Covered".equals(s)||"Completed".equals(s)||"Available".equals(s))return GREEN_SOFT;
        if("Current".equals(s)||"Confirmed".equals(s)||"Scheduled".equals(s))return Color.rgb(239,246,255);
        if("Next".equals(s)||"Revisit".equals(s))return ORANGE_SOFT;
        if("Resource Ready".equals(s))return Color.rgb(245,243,255);
        if("Cancelled".equals(s)||"Declined".equals(s))return Color.rgb(254,242,242);
        return Color.rgb(248,250,252);
    }
    private String initials(String s){if(s==null||s.trim().isEmpty())return "?";String[] p=s.trim().split("\\s+");return p.length==1?p[0].substring(0,1).toUpperCase():(p[0].substring(0,1)+p[p.length-1].substring(0,1)).toUpperCase();}
    private String shortTime(String s){return s==null||s.length()<5?"Time not recorded":s.substring(0,5);}
    private String nz(String s){return s==null||s.equals("null")||s.isEmpty()?"Not recorded":s;}
    private String greeting(){int h=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);return h<12?"Good morning":h<17?"Good afternoon":"Good evening";}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}
