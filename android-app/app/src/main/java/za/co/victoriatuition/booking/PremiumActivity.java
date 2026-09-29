package za.co.victoriatuition.booking;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.*;
import android.provider.OpenableColumns;
import android.text.InputType;
import android.view.*;
import android.widget.*;
import org.json.*;
import java.io.*;
import java.util.*;

public class PremiumActivity extends Activity {
    private VtaApi api;
    private SharedPreferences prefs;
    private JSONObject sync=new JSONObject();
    private String token="", roleChoice="", tab="Home";
    private boolean dark=false;
    private FrameLayout host;
    private LinearLayout nav;
    private JSONObject pendingStudent;
    private String pendingTitle="", pendingType="", pendingAccess="";
    private boolean pendingFeatured=false;
    private static final int FILE_PICK=7007;

    private int bg(){return dark?Color.rgb(9,15,27):Color.rgb(247,248,251);}
    private int surface(){return dark?Color.rgb(18,27,43):Color.WHITE;}
    private int surface2(){return dark?Color.rgb(27,39,59):Color.rgb(250,250,252);}
    private int ink(){return dark?Color.rgb(242,245,249):Color.rgb(19,32,54);}
    private int muted(){return dark?Color.rgb(150,164,184):Color.rgb(101,116,139);}
    private int line(){return dark?Color.rgb(48,62,82):Color.rgb(228,232,238);}
    private int navy(){return Color.rgb(11,43,99);}
    private int orange(){return Color.rgb(238,151,116);}
    private int orangeStrong(){return Color.rgb(229,77,46);}
    private int green(){return Color.rgb(90,154,74);}
    private int blue(){return Color.rgb(64,145,211);}
    private int purple(){return Color.rgb(124,92,190);}
    private int red(){return Color.rgb(200,55,70);}

    @Override public void onCreate(Bundle b){
        super.onCreate(b);
        prefs=getSharedPreferences("vta_cloud_app",MODE_PRIVATE);
        token=prefs.getString("token","");
        dark=prefs.getBoolean("premium_dark",false);
        api=new VtaApi(this);api.setToken(token);
        try{sync=new JSONObject(prefs.getString("cache","{}"));}catch(Exception ignored){}
        if(token.isEmpty())welcome();else refresh(()->shell());
    }

    private JSONObject j(Object...kv){JSONObject o=new JSONObject();try{for(int i=0;i+1<kv.length;i+=2)o.put(String.valueOf(kv[i]),kv[i+1]);}catch(Exception ignored){}return o;}
    private void cache(){prefs.edit().putString("cache",sync.toString()).apply();}
    private void refresh(Runnable done){
        api.post(j("action","sync"),true,(r,e)->{
            if(e!=null){if(sync.length()>0){toast("Offline — showing saved data");if(done!=null)done.run();}else{token="";prefs.edit().remove("token").apply();welcome();}return;}
            sync=r;cache();if(done!=null)done.run();
        });
    }
    private void act(JSONObject q,Runnable done){api.post(q,true,(r,e)->{if(e!=null){toast(e.getMessage());return;}refresh(done);});}

    private void welcome(){
        getWindow().setStatusBarColor(bg());getWindow().setNavigationBarColor(bg());
        ScrollView sv=new ScrollView(this);LinearLayout root=col();root.setPadding(dp(24),dp(30),dp(24),dp(36));root.setBackgroundColor(bg());sv.addView(root);setContentView(sv);
        LinearLayout brand=row();brand.setGravity(Gravity.CENTER_VERTICAL);
        ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.vta_logo_mark);logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);brand.addView(logo,new LinearLayout.LayoutParams(dp(64),dp(64)));
        LinearLayout bt=col();bt.setPadding(dp(12),0,0,0);bt.addView(t("Victoria Tuition",24,navy(),true));bt.addView(t("Academy",24,navy(),true));bt.addView(t("LEARN  •  GROW  •  ACHIEVE",9,muted(),true));brand.addView(bt,new LinearLayout.LayoutParams(0,-2,1));root.addView(brand);

        LinearLayout hero=box();hero.setPadding(dp(22),dp(24),dp(22),dp(24));hero.setBackground(grad(navy(),Color.rgb(22,58,119),24));
        hero.addView(pill("PRIVATE LEARNING SPACE",Color.rgb(39,70,126),Color.WHITE));
        hero.addView(t("Everything for your\nlearning, beautifully organised.",30,Color.WHITE,true),mtb(18,8));
        hero.addView(t("Lessons, progress, booking, resources and tutor support in one calm space.",14,Color.rgb(220,229,244),false));
        root.addView(hero,mtb(28,24));

        root.addView(t("Continue as",17,ink(),true),mb(10));
        roleCard(root,"Admin","Run the academy and update every learner.","ADMIN",orangeStrong());
        roleCard(root,"Student","Learn, book, practise and chat with Victoria.","STUDENT",blue());
        roleCard(root,"Parent","Follow progress and lesson information.","PARENT",green());
    }

    private void roleCard(LinearLayout root,String title,String sub,String role,int accent){
        LinearLayout c=row();c.setGravity(Gravity.CENTER_VERTICAL);c.setPadding(dp(16),dp(15),dp(14),dp(15));c.setBackground(round(surface(),18,1,line()));
        TextView mark=t("•",34,accent,true);mark.setGravity(Gravity.CENTER);c.addView(mark,new LinearLayout.LayoutParams(dp(34),dp(44)));
        LinearLayout txt=col();txt.addView(t(title,16,ink(),true));txt.addView(t(sub,11,muted(),false));c.addView(txt,new LinearLayout.LayoutParams(0,-2,1));c.addView(t("›",26,muted(),false));
        c.setOnClickListener(v->{roleChoice=role;login();});root.addView(c,mb(9));
    }

    private void login(){
        ScrollView sv=new ScrollView(this);LinearLayout root=col();root.setPadding(dp(24),dp(28),dp(24),dp(36));root.setBackgroundColor(bg());sv.addView(root);setContentView(sv);
        TextView back=t("‹  Back",14,ink(),true);back.setOnClickListener(v->welcome());root.addView(back,mb(28));
        ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.vta_logo_mark);logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);LinearLayout.LayoutParams ilp=new LinearLayout.LayoutParams(dp(76),dp(76));ilp.gravity=Gravity.CENTER_HORIZONTAL;root.addView(logo,ilp);
        TextView title=t(roleChoice.equals("ADMIN")?"Welcome back, Victoria":"Welcome back",28,ink(),true);title.setGravity(Gravity.CENTER);root.addView(title,mtb(12,4));
        TextView sub=t("Sign in to your Victoria Tuition Academy portal.",13,muted(),false);sub.setGravity(Gravity.CENTER);root.addView(sub,mb(24));
        LinearLayout card=box();EditText user=input("Username",false),pass=input("Password",true);card.addView(user);card.addView(pass,mtb(10,0));TextView err=t("",11,red(),true);card.addView(err,mtb(8,0));Button go=primary("Sign in");card.addView(go,mtb(14,0));root.addView(card);
        go.setOnClickListener(v->{go.setEnabled(false);go.setText("Signing in…");api.post(j("action","login","username",user.getText().toString().trim(),"password",pass.getText().toString()),false,(r,e)->{
            go.setEnabled(true);go.setText("Sign in");if(e!=null){err.setText(e.getMessage());return;}
            JSONObject u=r.optJSONObject("user");if(u==null||!roleChoice.equals(u.optString("role"))){err.setText("This account belongs to a different role.");return;}
            token=r.optString("token");api.setToken(token);prefs.edit().putString("token",token).apply();refresh(()->shell());
        });});
    }

    private void shell(){
        JSONObject u=sync.optJSONObject("user");if(u==null){welcome();return;}
        getWindow().setStatusBarColor(surface());getWindow().setNavigationBarColor(surface());
        LinearLayout root=col();root.setBackgroundColor(bg());
        LinearLayout top=row();top.setGravity(Gravity.CENTER_VERTICAL);top.setPadding(dp(16),dp(10),dp(12),dp(10));top.setBackgroundColor(surface());
        ImageView logo=new ImageView(this);logo.setImageResource(R.drawable.vta_logo_mark);logo.setScaleType(ImageView.ScaleType.CENTER_INSIDE);top.addView(logo,new LinearLayout.LayoutParams(dp(42),dp(42)));
        LinearLayout brand=col();brand.setPadding(dp(8),0,0,0);brand.addView(t("Victoria Tuition Academy",14,ink(),true));brand.addView(t("Live workspace",9,green(),true));top.addView(brand,new LinearLayout.LayoutParams(0,-2,1));
        TextView rf=pill("↻",surface2(),ink());rf.setTextSize(17);rf.setOnClickListener(v->refresh(()->showTab(tab)));top.addView(rf);
        root.addView(top);
        host=new FrameLayout(this);root.addView(host,new LinearLayout.LayoutParams(-1,0,1));
        nav=row();nav.setPadding(dp(6),dp(5),dp(6),dp(6));nav.setBackgroundColor(surface());root.addView(nav);
        setContentView(root);tab="Home";buildNav();showTab(tab);
    }

    private void buildNav(){
        nav.removeAllViews();JSONObject u=sync.optJSONObject("user");String role=u.optString("role");
        String[] labels=role.equals("ADMIN")?new String[]{"Home","Students","Schedule","Chat","More"}:role.equals("PARENT")?new String[]{"Home","Learn","Chat","More"}:new String[]{"Home","Learn","Book","Chat","More"};
        String[] icons=role.equals("ADMIN")?new String[]{"⌂","◎","▦","●","•••"}:role.equals("PARENT")?new String[]{"⌂","◇","●","•••"}:new String[]{"⌂","◇","+","●","•••"};
        for(int i=0;i<labels.length;i++){final String label=labels[i];boolean active=label.equals(tab);LinearLayout item=col();item.setGravity(Gravity.CENTER);item.setPadding(dp(5),dp(5),dp(5),dp(5));if(active)item.setBackground(round(dark?Color.rgb(58,43,38):Color.rgb(255,244,239),16,0,0));item.addView(t(icons[i],17,active?orangeStrong():muted(),true));item.addView(t(label,9,active?orangeStrong():muted(),true));item.setOnClickListener(v->{tab=label;buildNav();showTab(label);});nav.addView(item,new LinearLayout.LayoutParams(0,dp(55),1));}
    }

    private void showTab(String which){
        host.removeAllViews();JSONObject u=sync.optJSONObject("user");String role=u.optString("role");View v;
        if(role.equals("ADMIN")){
            if(which.equals("Students"))v=adminStudents();
            else if(which.equals("Schedule"))v=adminSchedule();
            else if(which.equals("Chat"))v=adminConversations();
            else if(which.equals("More"))v=adminMore();
            else v=adminHome();
        }else{
            if(which.equals("Learn"))v=studentLearn();
            else if(which.equals("Book"))v=studentBook();
            else if(which.equals("Chat"))v=chat(null,false);
            else if(which.equals("More"))v=studentMore();
            else v=studentHome();
        }
        host.addView(v,new FrameLayout.LayoutParams(-1,-1));
    }

    private View adminHome(){
        LinearLayout b=page();JSONObject u=sync.optJSONObject("user");
        b.addView(t("ADMIN WORKSPACE",10,orangeStrong(),true));b.addView(t(greeting()+", "+u.optString("displayName"),29,ink(),true),mtb(4,2));b.addView(t(longDate(),12,muted(),false),mb(18));
        LinearLayout hero=box();hero.setBackground(grad(navy(),Color.rgb(25,60,119),22));hero.addView(t("YOUR DAY",10,Color.rgb(189,205,232),true));hero.addView(t(upcomingCount()+" upcoming lessons",27,Color.WHITE,true),mtb(5,2));hero.addView(t(pendingCount()+" booking requests need attention",12,Color.rgb(224,231,244),false));b.addView(hero,mb(12));
        LinearLayout stats=row();stats.addView(metric("Students",String.valueOf(arr("students").length()),blue()),new LinearLayout.LayoutParams(0,-2,1));stats.addView(metric("Completed value","R"+completedValue(),green()),w(1,8));b.addView(stats);
        section(b,"Next up");int n=0;for(JSONObject l:jsonList(arr("lessons"))){if(!"Completed".equals(l.optString("status"))&&n<4){b.addView(lessonCard(l,true),mb(8));n++;}}if(n==0)b.addView(empty("No upcoming lessons."));
        section(b,"Quick actions");LinearLayout q=row();q.addView(actionTile("Student","Add learner",orangeStrong(),v->newStudent()),new LinearLayout.LayoutParams(0,-2,1));q.addView(actionTile("Lesson","Add or log",blue(),v->lessonDialog(null)),w(1,8));b.addView(q,mb(8));LinearLayout q2=row();q2.addView(actionTile("Resource","Upload",purple(),v->chooseStudentForUpload()),new LinearLayout.LayoutParams(0,-2,1));q2.addView(actionTile("Update","Announcement",green(),v->announcement()),w(1,8));b.addView(q2);
        return scroll(b);
    }

    private View adminStudents(){
        LinearLayout b=page();title(b,"Students","Open a learner to edit their profile, syllabus, next topics and resources.");
        Button add=primary("Add student");add.setOnClickListener(v->newStudent());b.addView(add,mb(14));
        JSONArray students=arr("students");
        for(int i=0;i<students.length();i++){JSONObject s=students.optJSONObject(i);JSONArray topics=topicsFor(s.optString("id"));LinearLayout c=box();LinearLayout top=row();top.setGravity(Gravity.CENTER_VERTICAL);
            TextView av=t(initials(s.optString("display_name")),15,Color.WHITE,true);av.setGravity(Gravity.CENTER);av.setBackground(circle(colorFor(i)));top.addView(av,new LinearLayout.LayoutParams(dp(48),dp(48)));
            LinearLayout txt=col();txt.setPadding(dp(12),0,0,0);txt.addView(t(s.optString("display_name"),17,ink(),true));txt.addView(t(nz(s.optString("grade"))+"  •  "+nz(s.optString("subjects")),11,muted(),false));top.addView(txt,new LinearLayout.LayoutParams(0,-2,1));top.addView(t("›",25,muted(),false));c.addView(top);
            int covered=countStatus(topics,"Covered")+countStatus(topics,"Completed"),total=topics.length();c.addView(progressLine(covered,total),mtb(12,4));c.addView(t(covered+" of "+total+" syllabus topics covered",10,muted(),false));c.setOnClickListener(v->studentProfile(s));b.addView(c,mb(9));
        }
        return scroll(b);
    }

    private void studentProfile(JSONObject s){
        LinearLayout b=page();TextView back=t("‹  Students",13,muted(),true);back.setOnClickListener(v->{tab="Students";buildNav();showTab(tab);});b.addView(back,mb(14));
        LinearLayout hero=box();LinearLayout top=row();TextView av=t(initials(s.optString("display_name")),18,Color.WHITE,true);av.setGravity(Gravity.CENTER);av.setBackground(circle(navy()));top.addView(av,new LinearLayout.LayoutParams(dp(58),dp(58)));LinearLayout tx=col();tx.setPadding(dp(13),0,0,0);tx.addView(t(s.optString("display_name"),23,ink(),true));tx.addView(t(nz(s.optString("grade"))+"  •  "+nz(s.optString("curriculum")),11,muted(),false));tx.addView(t(nz(s.optString("subjects")),11,orangeStrong(),true));top.addView(tx,new LinearLayout.LayoutParams(0,-2,1));hero.addView(top);
        JSONArray topics=topicsFor(s.optString("id"));int covered=countStatus(topics,"Covered")+countStatus(topics,"Completed");hero.addView(progressLine(covered,topics.length()),mtb(14,5));hero.addView(t(covered+" / "+topics.length()+" topics covered",11,muted(),false));b.addView(hero,mb(12));
        LinearLayout actions=row();Button edit=secondary("Edit profile");edit.setOnClickListener(v->editStudent(s));actions.addView(edit,new LinearLayout.LayoutParams(0,dp(46),1));Button msg=secondary("Message");msg.setOnClickListener(v->openAdminChat(s.optString("id")));actions.addView(msg,wh(1,8,46));b.addView(actions,mb(8));Button add=primary("Add syllabus topic");add.setOnClickListener(v->editTopic(s,null));b.addView(add,mb(14));
        section(b,"Next lessons");int nx=0;for(JSONObject x:jsonList(topics)){if("Next".equals(x.optString("status"))||"Current".equals(x.optString("status"))){b.addView(topicCard(x,true,s),mb(7));nx++;}}if(nx==0)b.addView(empty("No next topic selected yet."));
        section(b,"Full syllabus");String last="";for(JSONObject x:jsonList(topics)){String term=x.optString("term_label");if(!term.equals(last)){b.addView(t(term.isEmpty()?"Other":term,11,muted(),true),mtb(8,6));last=term;}b.addView(topicCard(x,true,s),mb(6));}
        section(b,"Resources");resourceCategoryStrip(b,s.optString("id"),true);
        host.removeAllViews();host.addView(scroll(b));
    }

    private View topicCard(JSONObject x,boolean editable,JSONObject student){
        LinearLayout c=row();c.setGravity(Gravity.CENTER_VERTICAL);c.setPadding(dp(13),dp(12),dp(13),dp(12));c.setBackground(round(surface(),15,1,line()));LinearLayout tx=col();tx.addView(t(x.optString("topic"),13,ink(),true));tx.addView(t(x.optString("strand"),9,muted(),false));c.addView(tx,new LinearLayout.LayoutParams(0,-2,1));c.addView(statusChip(x.optString("status")));if(editable)c.setOnClickListener(v->editTopic(student,x));return c;
    }

    private void editStudent(JSONObject s){
        LinearLayout b=form();EditText name=input("Student name",false),grade=input("Grade",false),cur=input("Curriculum",false),sub=input("Subjects",false),note=input("Profile note",false);name.setText(s.optString("display_name"));grade.setText(s.optString("grade"));cur.setText(s.optString("curriculum"));sub.setText(s.optString("subjects"));note.setText(s.optString("profile_note"));for(EditText e:new EditText[]{name,grade,cur,sub,note})b.addView(e,mb(8));
        new AlertDialog.Builder(this).setTitle("Edit student profile").setView(b).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->act(j("action","studentUpdate","studentId",s.optString("id"),"name",name.getText().toString(),"grade",grade.getText().toString(),"curriculum",cur.getText().toString(),"subjects",sub.getText().toString(),"profileNote",note.getText().toString(),"active",true),()->{toast("Profile updated");JSONObject ns=findStudent(s.optString("id"));studentProfile(ns==null?s:ns);})).show();
    }

    private void editTopic(JSONObject student,JSONObject x){
        LinearLayout b=form();EditText subject=input("Subject",false),strand=input("Strand / section",false),term=input("Term",false),topic=input("Topic",false),notes=input("Tutor notes",false),order=input("Sort order",false);Spinner status=new Spinner(this);String[] sts={"Covered","Current","Next","Revisit","Resource Ready","Future"};status.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,sts));
        if(x!=null){subject.setText(x.optString("subject"));strand.setText(x.optString("strand"));term.setText(x.optString("term_label"));topic.setText(x.optString("topic"));notes.setText(x.optString("notes"));order.setText(String.valueOf(x.optInt("sort_order")));for(int i=0;i<sts.length;i++)if(sts[i].equals(x.optString("status")))status.setSelection(i);}else{subject.setText(student.optString("subjects").contains("Natural")?"Natural Sciences":student.optString("subjects"));order.setText(String.valueOf(topicsFor(student.optString("id")).length()*10+10));}
        for(View v:new View[]{subject,strand,term,topic,status,notes,order})b.addView(v,mb(8));
        AlertDialog d=new AlertDialog.Builder(this).setTitle(x==null?"Add syllabus topic":"Edit syllabus topic").setView(b).setNegativeButton("Cancel",null).setPositiveButton("Save",null).create();
        if(x!=null)d.setButton(AlertDialog.BUTTON_NEUTRAL,"Delete",(DialogInterface.OnClickListener)null);
        d.setOnShowListener(v->{d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(z->{if(topic.getText().toString().trim().isEmpty()){toast("Enter a topic.");return;}JSONObject q=j("action","topicUpsert","studentId",student.optString("id"),"subject",subject.getText().toString(),"strand",strand.getText().toString(),"termLabel",term.getText().toString(),"topic",topic.getText().toString(),"status",status.getSelectedItem().toString(),"notes",notes.getText().toString(),"sortOrder",parseInt(order.getText().toString(),0));if(x!=null)try{q.put("id",x.optString("id"));}catch(Exception ignored){}act(q,()->{d.dismiss();studentProfile(findStudent(student.optString("id")));});});if(x!=null){Button del=d.getButton(AlertDialog.BUTTON_NEUTRAL);del.setTextColor(red());del.setOnClickListener(z->new AlertDialog.Builder(this).setTitle("Delete topic?").setMessage(x.optString("topic")).setNegativeButton("Cancel",null).setPositiveButton("Delete",(dd,ww)->act(j("action","topicDelete","id",x.optString("id")),()->{d.dismiss();studentProfile(findStudent(student.optString("id")));})).show());}});d.show();
    }

    private View adminSchedule(){
        LinearLayout b=page();title(b,"Schedule","Confirm requests and manage lessons without exposing private details to students.");
        LinearLayout a=row();Button l=primary("Add lesson");l.setOnClickListener(v->lessonDialog(null));a.addView(l,new LinearLayout.LayoutParams(0,dp(46),1));Button block=secondary("Block time");block.setOnClickListener(v->blockDialog());a.addView(block,wh(1,8,46));b.addView(a,mb(14));
        section(b,"Requests");int n=0;for(JSONObject x:jsonList(arr("bookings"))){if("Pending".equals(x.optString("status"))){b.addView(requestCard(x),mb(8));n++;}}if(n==0)b.addView(empty("No pending requests."));
        section(b,"Lessons");for(JSONObject lsn:jsonList(arr("lessons")))b.addView(lessonCard(lsn,true),mb(7));
        return scroll(b);
    }

    private View requestCard(JSONObject x){
        LinearLayout c=box();c.addView(t(studentName(x.optString("student_id")),14,ink(),true));c.addView(t(x.optString("requested_date")+"  •  "+shortTime(x.optString("requested_time"))+"  •  "+x.optInt("duration_minutes")+" min",12,orangeStrong(),true),mtb(6,2));c.addView(t(x.optString("subject")+" — "+x.optString("topic"),12,muted(),false));LinearLayout a=row();Button yes=small("Confirm");yes.setOnClickListener(v->act(j("action","bookingStatus","id",x.optString("id"),"status","Confirmed"),()->showTab("Schedule")));Button no=small("Decline");no.setOnClickListener(v->act(j("action","bookingStatus","id",x.optString("id"),"status","Declined"),()->showTab("Schedule")));a.addView(yes,new LinearLayout.LayoutParams(0,dp(42),1));a.addView(no,wh(1,8,42));c.addView(a,mtb(10,0));return c;
    }

    private View adminConversations(){
        LinearLayout b=page();title(b,"Chat","Student conversations, kept compact like a real messenger.");
        for(JSONObject s:jsonList(arr("students"))){JSONObject last=lastMessage(s.optString("id"));LinearLayout c=row();c.setGravity(Gravity.CENTER_VERTICAL);c.setPadding(dp(13),dp(12),dp(13),dp(12));c.setBackground(round(surface(),16,1,line()));TextView av=t(initials(s.optString("display_name")),14,Color.WHITE,true);av.setGravity(Gravity.CENTER);av.setBackground(circle(navy()));c.addView(av,new LinearLayout.LayoutParams(dp(44),dp(44)));LinearLayout tx=col();tx.setPadding(dp(11),0,0,0);tx.addView(t(s.optString("display_name"),14,ink(),true));tx.addView(t(last==null?"No messages yet":last.optString("body"),11,muted(),false));c.addView(tx,new LinearLayout.LayoutParams(0,-2,1));c.addView(t("›",24,muted(),false));c.setOnClickListener(v->openAdminChat(s.optString("id")));b.addView(c,mb(8));}
        return scroll(b);
    }

    private void openAdminChat(String studentId){host.removeAllViews();host.addView(chat(studentId,true));}

    private View chat(String studentId,boolean admin){
        if(!admin)studentId=sync.optJSONObject("user").optString("id");final String sid=studentId;
        LinearLayout root=col();root.setBackgroundColor(dark?Color.rgb(12,20,29):Color.rgb(239,243,241));
        LinearLayout head=row();head.setGravity(Gravity.CENTER_VERTICAL);head.setPadding(dp(12),dp(10),dp(12),dp(10));head.setBackgroundColor(dark?Color.rgb(17,38,43):Color.rgb(0,92,75));if(admin){TextView back=t("‹",28,Color.WHITE,false);back.setOnClickListener(v->{tab="Chat";buildNav();showTab(tab);});head.addView(back,new LinearLayout.LayoutParams(dp(38),dp(42)));}
        TextView av=t(admin?initials(studentName(sid)):"V",14,Color.WHITE,true);av.setGravity(Gravity.CENTER);av.setBackground(circle(admin?navy():orangeStrong()));head.addView(av,new LinearLayout.LayoutParams(dp(42),dp(42)));LinearLayout tx=col();tx.setPadding(dp(10),0,0,0);tx.addView(t(admin?studentName(sid):"Victoria",15,Color.WHITE,true));tx.addView(t("Tutor chat",10,Color.rgb(201,241,229),false));head.addView(tx,new LinearLayout.LayoutParams(0,-2,1));root.addView(head);
        ScrollView sv=new ScrollView(this);LinearLayout msgs=col();msgs.setPadding(dp(10),dp(12),dp(10),dp(12));sv.addView(msgs);root.addView(sv,new LinearLayout.LayoutParams(-1,0,1));
        String me=sync.optJSONObject("user").optString("id");List<JSONObject> list=new ArrayList<>();for(JSONObject m:jsonList(arr("messages")))if(sid.equals(m.optString("student_id")))list.add(m);Collections.reverse(list);for(JSONObject m:list)msgs.addView(bubble(m,me.equals(m.optString("sender_id"))),mb(6));if(list.isEmpty()){TextView e=t("No messages yet.",12,muted(),false);e.setGravity(Gravity.CENTER);msgs.addView(e,mtb(30,0));}
        LinearLayout comp=row();comp.setGravity(Gravity.BOTTOM);comp.setPadding(dp(8),dp(7),dp(8),dp(9));comp.setBackgroundColor(surface());EditText in=input("Message",false);in.setSingleLine(false);in.setMaxLines(4);comp.addView(in,new LinearLayout.LayoutParams(0,-2,1));TextView send=t("➤",20,Color.WHITE,true);send.setGravity(Gravity.CENTER);send.setBackground(circle(Color.rgb(0,168,132)));comp.addView(send,new LinearLayout.LayoutParams(dp(46),dp(46)));send.setOnClickListener(v->{String msg=in.getText().toString().trim();if(msg.isEmpty())return;in.setText("");JSONObject q=j("action","messageSend","message",msg);if(admin)try{q.put("studentId",sid);}catch(Exception ignored){}act(q,()->{host.removeAllViews();host.addView(chat(sid,admin));});});root.addView(comp);return root;
    }

    private View bubble(JSONObject m,boolean mine){
        LinearLayout wrap=row();wrap.setGravity(mine?Gravity.RIGHT:Gravity.LEFT);LinearLayout b=col();b.setPadding(dp(11),dp(8),dp(10),dp(6));b.setBackground(round(mine?(dark?Color.rgb(21,76,62):Color.rgb(217,253,211)):(dark?Color.rgb(31,42,52):Color.WHITE),14,0,0));TextView msg=t(m.optString("body"),14,ink(),false);msg.setMaxWidth((int)(getResources().getDisplayMetrics().widthPixels*.72));b.addView(msg);TextView ts=t(messageTime(m.optString("created_at")),9,muted(),false);ts.setGravity(Gravity.RIGHT);b.addView(ts,mtb(4,0));wrap.addView(b,new LinearLayout.LayoutParams(-2,-2));return wrap;
    }

    private View adminMore(){
        LinearLayout b=page();title(b,"More","Resources, finance and app preferences.");
        menu(b,"Resources","Interactive lessons, slides, notes and worksheets",purple(),v->showResources(null,true));
        menu(b,"Finance","Completed value, payments and invoices",green(),v->showFinance());
        menu(b,"Appearance",dark?"Dark mode":"Light mode",blue(),v->{dark=!dark;prefs.edit().putBoolean("premium_dark",dark).apply();shell();});
        menu(b,"Log out","Sign out on this device",red(),v->logout());
        return scroll(b);
    }

    private void showResources(String studentId,boolean admin){
        LinearLayout b=page();TextView back=t("‹  Back",13,muted(),true);back.setOnClickListener(v->{if(admin){tab="More";buildNav();showTab(tab);}else{tab="More";buildNav();showTab(tab);}});b.addView(back,mb(12));title(b,admin?"Resources":"My resources","Choose a category instead of scrolling through one long page.");
        if(admin){Button up=primary("Upload resource");up.setOnClickListener(v->chooseStudentForUpload());b.addView(up,mb(14));}
        resourceCategoryStrip(b,studentId,admin);host.removeAllViews();host.addView(scroll(b));
    }

    private void resourceCategoryStrip(LinearLayout b,String studentId,boolean admin){
        String[] cats={"Interactive Lesson","Slides","PDF / Notes","Worksheet","Video","Image","Other"};int[] colors={purple(),blue(),orangeStrong(),green(),navy(),Color.rgb(193,117,67),muted()};
        GridLayout grid=new GridLayout(this);grid.setColumnCount(2);
        for(int i=0;i<cats.length;i++){String cat=cats[i];int count=resourceCount(cat,studentId);LinearLayout c=box();c.setPadding(dp(14),dp(14),dp(14),dp(14));c.addView(t(cat,13,ink(),true));c.addView(t(count+" item"+(count==1?"":"s"),10,colors[i],true),mtb(5,0));c.setOnClickListener(v->resourceCategory(cat,studentId,admin));GridLayout.LayoutParams lp=new GridLayout.LayoutParams();lp.width=0;lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);lp.setMargins(dp(4),dp(4),dp(4),dp(4));c.setLayoutParams(lp);grid.addView(c);}b.addView(grid);
    }

    private void resourceCategory(String cat,String studentId,boolean admin){
        LinearLayout b=page();TextView back=t("‹  Resources",13,muted(),true);back.setOnClickListener(v->showResources(studentId,admin));b.addView(back,mb(12));title(b,cat,"Open a resource for details and access information.");
        int count=0;for(JSONObject r:jsonList(arr("resources"))){if(!cat.equals(r.optString("resource_type")))continue;if(studentId!=null&&!studentId.equals(r.optString("student_id")))continue;b.addView(resourceCard(r,admin),mb(8));count++;}if(count==0)b.addView(empty("Nothing in this category yet."));host.removeAllViews();host.addView(scroll(b));
    }

    private View resourceCard(JSONObject r,boolean admin){
        LinearLayout c=box();LinearLayout top=row();top.addView(statusChip(r.optString("completion_status")),new LinearLayout.LayoutParams(-2,-2));if(r.optBoolean("featured")){TextView f=pill("FEATURED",dark?Color.rgb(67,48,31):Color.rgb(255,246,230),orangeStrong());top.addView(f,ml(7));}c.addView(top);c.addView(t(r.optString("title"),16,ink(),true),mtb(9,2));if(admin)c.addView(t(studentName(r.optString("student_id")),10,muted(),true));if(!r.optString("description").isEmpty())c.addView(t(r.optString("description"),11,muted(),false),mtb(4,0));if(!r.optString("access_note").isEmpty())c.addView(accessNote(r.optString("access_note")),mtb(9,0));LinearLayout actions=row();Button open=small("Open");open.setOnClickListener(v->openResource(r));actions.addView(open,new LinearLayout.LayoutParams(0,dp(42),1));if(admin){Button edit=small("Edit");edit.setOnClickListener(v->editResource(r));actions.addView(edit,wh(1,8,42));}c.addView(actions,mtb(10,0));return c;
    }

    private View accessNote(String s){TextView n=t(s,11,ink(),true);n.setPadding(dp(11),dp(9),dp(11),dp(9));n.setBackground(round(dark?Color.rgb(47,38,31):Color.rgb(255,248,240),12,1,dark?Color.rgb(87,61,46):Color.rgb(250,218,196)));return n;}

    private void editResource(JSONObject r){
        LinearLayout b=form();EditText title=input("Title",false),desc=input("Description",false),access=input("Access note / password",false);title.setText(r.optString("title"));desc.setText(r.optString("description"));access.setText(r.optString("access_note"));Spinner type=new Spinner(this);String[] types={"Interactive Lesson","Slides","PDF / Notes","Worksheet","Video","Image","Other"};type.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,types));for(int i=0;i<types.length;i++)if(types[i].equals(r.optString("resource_type")))type.setSelection(i);Spinner state=new Spinner(this);String[] states={"Available","Completed","Assigned","Archived"};state.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,states));CheckBox featured=new CheckBox(this);featured.setText("Feature this resource");featured.setChecked(r.optBoolean("featured"));for(View v:new View[]{title,type,desc,access,state,featured})b.addView(v,mb(8));
        new AlertDialog.Builder(this).setTitle("Edit resource").setView(b).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->act(j("action","resourceUpdate","id",r.optString("id"),"title",title.getText().toString(),"resourceType",type.getSelectedItem().toString(),"description",desc.getText().toString(),"accessNote",access.getText().toString(),"completionStatus",state.getSelectedItem().toString(),"featured",featured.isChecked()),()->resourceCategory(type.getSelectedItem().toString(),null,true))).show();
    }

    private void showFinance(){
        LinearLayout b=page();TextView back=t("‹  More",13,muted(),true);back.setOnClickListener(v->{tab="More";buildNav();showTab(tab);});b.addView(back,mb(12));title(b,"Finance","Verified completed lesson value and payment records.");LinearLayout hero=box();hero.setBackground(grad(navy(),Color.rgb(22,58,119),22));hero.addView(t("COMPLETED LESSON VALUE",10,Color.rgb(196,210,234),true));hero.addView(t("R"+completedValue(),30,Color.WHITE,true));hero.addView(t("R150 per completed teaching hour",11,Color.rgb(222,231,246),false));b.addView(hero,mb(12));LinearLayout a=row();Button p=secondary("Record payment");p.setOnClickListener(v->paymentDialog());a.addView(p,new LinearLayout.LayoutParams(0,dp(45),1));Button inv=primary("Create invoice");inv.setOnClickListener(v->invoiceDialog());a.addView(inv,wh(1,8,45));b.addView(a,mb(14));section(b,"Payments");for(JSONObject x:jsonList(arr("payments"))){LinearLayout c=box();c.addView(t(x.optString("payer"),14,ink(),true));c.addView(t(x.optString("payment_date"),10,muted(),false));Object cents=x.opt("amount_cents");c.addView(t(cents==null||cents==JSONObject.NULL?"Amount not entered":"R"+x.optLong("amount_cents")/100,15,cents==null||cents==JSONObject.NULL?orangeStrong():green(),true));b.addView(c,mb(7));}host.removeAllViews();host.addView(scroll(b));
    }

    private View studentHome(){
        LinearLayout b=page();JSONObject u=sync.optJSONObject("user");b.addView(t("MY LEARNING SPACE",10,orangeStrong(),true));b.addView(t("Hi, "+u.optString("displayName"),29,ink(),true),mtb(4,2));b.addView(t(nz(u.optString("grade"))+"  •  "+nz(u.optString("subjects")),12,muted(),false),mb(16));
        JSONArray topics=arr("topicProgress");int covered=countStatus(topics,"Covered")+countStatus(topics,"Completed");LinearLayout progress=box();progress.addView(t("Your progress",14,ink(),true));progress.addView(t(percent(covered,topics.length())+"%",30,navy(),true),mtb(7,1));progress.addView(progressLine(covered,topics.length()),mtb(6,4));progress.addView(t(covered+" of "+topics.length()+" syllabus topics covered",10,muted(),false));b.addView(progress,mb(12));
        section(b,"Next up");int nx=0;for(JSONObject x:jsonList(topics)){if(("Next".equals(x.optString("status"))||"Current".equals(x.optString("status")))&&nx<4){b.addView(topicCard(x,false,null),mb(7));nx++;}}if(nx==0)b.addView(empty("Victoria will choose your next topic soon."));
        section(b,"Next lesson");JSONObject next=nextLesson();if(next==null)b.addView(empty("No upcoming lesson yet."));else b.addView(lessonCard(next,false));
        section(b,"Featured resources");int n=0;for(JSONObject r:jsonList(arr("resources"))){if(r.optBoolean("featured")&&n<3){b.addView(resourceCard(r,false),mb(7));n++;}}
        return scroll(b);
    }

    private View studentLearn(){
        LinearLayout b=page();title(b,"My learning","See what is covered, what needs revision, and what comes next.");JSONArray topics=arr("topicProgress");int covered=countStatus(topics,"Covered")+countStatus(topics,"Completed");LinearLayout hero=box();hero.addView(t(percent(covered,topics.length())+"% syllabus progress",22,navy(),true));hero.addView(progressLine(covered,topics.length()),mtb(9,2));b.addView(hero,mb(12));
        section(b,"Your syllabus");String last="";for(JSONObject x:jsonList(topics)){String term=x.optString("term_label");if(!term.equals(last)){b.addView(t(term.isEmpty()?"Other":term,11,muted(),true),mtb(8,6));last=term;}b.addView(topicCard(x,false,null),mb(6));}
        section(b,"Resources");resourceCategoryStrip(b,null,false);return scroll(b);
    }

    private View studentBook(){
        LinearLayout b=page();title(b,"Book a lesson","Choose what you want help with, then pick a live available time.");LinearLayout hint=box();hint.addView(t("Not sure what to choose?",15,ink(),true));hint.addView(t("The suggestions below come from your syllabus progress, so you can simply tap what needs attention.",11,muted(),false));b.addView(hint,mb(12));
        section(b,"Recommended");List<JSONObject> rec=new ArrayList<>();for(JSONObject x:jsonList(arr("topicProgress")))if("Next".equals(x.optString("status"))||"Revisit".equals(x.optString("status"))||"Current".equals(x.optString("status")))rec.add(x);if(rec.isEmpty())for(JSONObject x:jsonList(arr("topicProgress")))if("Future".equals(x.optString("status")))rec.add(x);for(int i=0;i<Math.min(6,rec.size());i++){JSONObject x=rec.get(i);LinearLayout c=topicCard(x,false,null);c.setOnClickListener(v->bookingDialog(x.optString("topic"),x.optString("subject")));b.addView(c,mb(7));}
        Button other=secondary("Choose another topic");other.setOnClickListener(v->bookingDialog("",""));b.addView(other,mtb(7,8));b.addView(t("Other students’ names and lesson details are never shown in availability.",10,muted(),false));return scroll(b);
    }

    private View studentMore(){
        LinearLayout b=page();title(b,"More","Your resources, lessons and app preferences.");menu(b,"Resources","Interactive lessons, slides, notes and worksheets",purple(),v->showResources(null,false));menu(b,"Lesson history","Completed and upcoming lessons",blue(),v->lessonHistory());menu(b,"Appearance",dark?"Dark mode":"Light mode",green(),v->{dark=!dark;prefs.edit().putBoolean("premium_dark",dark).apply();shell();});menu(b,"Log out","Sign out on this device",red(),v->logout());return scroll(b);
    }

    private void lessonHistory(){LinearLayout b=page();TextView back=t("‹  More",13,muted(),true);back.setOnClickListener(v->{tab="More";buildNav();showTab(tab);});b.addView(back,mb(12));title(b,"My lessons","Your teaching history and upcoming lessons.");for(JSONObject x:jsonList(arr("lessons")))b.addView(lessonCard(x,false),mb(7));host.removeAllViews();host.addView(scroll(b));}

    private void bookingDialog(String suggested,String suggestedSubject){
        LinearLayout b=form();EditText topic=input("Topic",false),help=input("What exactly are you struggling with? (optional)",false);topic.setText(suggested);Spinner subject=new Spinner(this);String[] subjects={"Mathematics","Physics","Natural Sciences"};subject.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,subjects));for(int i=0;i<subjects.length;i++)if(subjects[i].equals(suggestedSubject))subject.setSelection(i);Spinner duration=new Spinner(this);duration.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"1 hour","2 hours"}));Button date=secondary("Choose date");GridLayout slots=new GridLayout(this);slots.setColumnCount(3);final String[] d={""},tm={""};final int[] mins={60};b.addView(t("1  What do you want help with?",11,orangeStrong(),true));b.addView(topic,mb(8));b.addView(subject,mb(12));b.addView(t("2  Lesson length and date",11,orangeStrong(),true));b.addView(duration,mb(8));b.addView(date,mb(12));b.addView(t("3  Choose an available time",11,orangeStrong(),true));b.addView(slots,mb(10));b.addView(help);
        Runnable load=()->{slots.removeAllViews();if(d[0].isEmpty()){slots.addView(t("Choose a date first.",10,muted(),false));return;}slots.addView(t("Checking…",10,muted(),false));api.post(j("action","availability","date",d[0],"duration",mins[0]),true,(r,e)->{slots.removeAllViews();if(e!=null){slots.addView(t(e.getMessage(),10,red(),false));return;}for(JSONObject s:jsonList(r.optJSONArray("slots"))){boolean free=s.optBoolean("available");Button x=new Button(this);x.setAllCaps(false);x.setText(s.optString("time")+"\n"+(free?"Available":"Unavailable"));x.setTextSize(9);x.setEnabled(free);x.setTextColor(free?ink():muted());x.setBackground(round(free?surface():surface2(),11,1,line()));GridLayout.LayoutParams lp=new GridLayout.LayoutParams();lp.width=0;lp.columnSpec=GridLayout.spec(GridLayout.UNDEFINED,1f);lp.setMargins(dp(3),dp(3),dp(3),dp(3));x.setLayoutParams(lp);if(free)x.setOnClickListener(v->{tm[0]=s.optString("time");for(int q=0;q<slots.getChildCount();q++){View y=slots.getChildAt(q);if(y instanceof Button&&y.isEnabled())y.setBackground(round(surface(),11,1,line()));}x.setBackground(round(dark?Color.rgb(75,45,35):Color.rgb(255,241,235),11,2,orangeStrong()));});slots.addView(x);}});};date.setOnClickListener(v->pickDate(date,d,load));duration.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){public void onItemSelected(AdapterView<?> p,View v,int pos,long id){mins[0]=pos==0?60:120;tm[0]="";load.run();}public void onNothingSelected(AdapterView<?> p){}});
        AlertDialog dialog=new AlertDialog.Builder(this).setTitle("Request a lesson").setView(b).setNegativeButton("Cancel",null).setPositiveButton("Send request",null).create();dialog.setOnShowListener(v->dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(z->{if(topic.getText().toString().trim().isEmpty()){toast("Choose a topic.");return;}if(d[0].isEmpty()||tm[0].isEmpty()){toast("Choose a date and available time.");return;}act(j("action","bookingCreate","date",d[0],"time",tm[0],"duration",mins[0],"subject",subject.getSelectedItem().toString(),"topic",topic.getText().toString(),"help",help.getText().toString()),()->{dialog.dismiss();toast("Request sent to Victoria");tab="Home";buildNav();showTab(tab);});}));dialog.show();
    }

    private void newStudent(){LinearLayout b=form();EditText name=input("Student name",false),grade=input("Grade",false),cur=input("Curriculum",false),sub=input("Subjects",false),user=input("Username",false),pass=input("Temporary password",true);for(EditText e:new EditText[]{name,grade,cur,sub,user,pass})b.addView(e,mb(8));new AlertDialog.Builder(this).setTitle("Add student").setView(b).setNegativeButton("Cancel",null).setPositiveButton("Create",(d,w)->act(j("action","studentCreate","name",name.getText().toString(),"grade",grade.getText().toString(),"curriculum",cur.getText().toString(),"subjects",sub.getText().toString(),"username",user.getText().toString(),"password",pass.getText().toString()),()->showTab("Students"))).show();}

    private void lessonDialog(JSONObject old){JSONArray students=arr("students");String[] names=new String[students.length()];for(int i=0;i<students.length();i++)names[i]=students.optJSONObject(i).optString("display_name");LinearLayout b=form();Spinner st=new Spinner(this);st.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,names));final String[] date={""},time={""};Button db=secondary("Choose date"),tb=secondary("Choose time");db.setOnClickListener(v->pickDate(db,date,()->{}));tb.setOnClickListener(v->pickTime(tb,time));Spinner dur=new Spinner(this);dur.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"60 minutes","120 minutes"}));EditText subject=input("Subject",false),topic=input("Topic",false),status=input("Status",false),notes=input("Notes",false);status.setText("Planned");for(View v:new View[]{st,db,tb,dur,subject,topic,status,notes})b.addView(v,mb(8));new AlertDialog.Builder(this).setTitle("Add lesson").setView(b).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{JSONObject s=students.optJSONObject(st.getSelectedItemPosition());act(j("action","lessonUpsert","studentId",s.optString("id"),"date",date[0],"time",time[0],"duration",dur.getSelectedItemPosition()==0?60:120,"subject",subject.getText().toString(),"topic",topic.getText().toString(),"status",status.getText().toString(),"notes",notes.getText().toString()),()->showTab("Schedule"));}).show();}

    private void blockDialog(){LinearLayout b=form();final String[] date={""},start={""},end={""};Button db=secondary("Choose date"),sb=secondary("Start"),eb=secondary("End");db.setOnClickListener(v->pickDate(db,date,()->{}));sb.setOnClickListener(v->pickTime(sb,start));eb.setOnClickListener(v->pickTime(eb,end));EditText reason=input("Private reason",false);for(View v:new View[]{db,sb,eb,reason})b.addView(v,mb(8));new AlertDialog.Builder(this).setTitle("Block time").setMessage("Students will only see this time as unavailable.").setView(b).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->act(j("action","scheduleCreate","date",date[0],"start",start[0],"end",end[0],"title",reason.getText().toString(),"visibility","PRIVATE"),()->showTab("Schedule"))).show();}

    private void announcement(){LinearLayout b=form();EditText title=input("Title",false),msg=input("Message",false);b.addView(title,mb(8));b.addView(msg);new AlertDialog.Builder(this).setTitle("Post announcement").setView(b).setNegativeButton("Cancel",null).setPositiveButton("Post",(d,w)->act(j("action","announcementCreate","title",title.getText().toString(),"message",msg.getText().toString(),"audience","ALL"),()->showTab("Home"))).show();}

    private void paymentDialog(){LinearLayout b=form();EditText payer=input("Payer",false),date=input("Date YYYY-MM-DD",false),amount=input("Amount in rand",false),note=input("Note",false);payer.setText("Tracey");amount.setInputType(InputType.TYPE_CLASS_NUMBER|InputType.TYPE_NUMBER_FLAG_DECIMAL);for(EditText e:new EditText[]{payer,date,amount,note})b.addView(e,mb(8));new AlertDialog.Builder(this).setTitle("Record payment").setView(b).setNegativeButton("Cancel",null).setPositiveButton("Save",(d,w)->{JSONObject q=j("action","paymentCreate","payer",payer.getText().toString(),"date",date.getText().toString(),"note",note.getText().toString());if(!amount.getText().toString().trim().isEmpty())try{q.put("amountCents",Math.round(Double.parseDouble(amount.getText().toString())*100));}catch(Exception ignored){}act(q,()->showFinance());}).show();}

    private void invoiceDialog(){LinearLayout b=form();EditText no=input("Invoice number",false),payer=input("Payer",false),start=input("Period start YYYY-MM-DD",false),end=input("Period end YYYY-MM-DD",false);payer.setText("Tracey");for(EditText e:new EditText[]{no,payer,start,end})b.addView(e,mb(8));new AlertDialog.Builder(this).setTitle("Create invoice").setView(b).setNegativeButton("Cancel",null).setPositiveButton("Create",(d,w)->act(j("action","invoiceCreate","invoiceNumber",no.getText().toString(),"payer",payer.getText().toString(),"periodStart",start.getText().toString(),"periodEnd",end.getText().toString(),"hourlyRateCents",15000),()->showFinance())).show();}

    private void chooseStudentForUpload(){JSONArray s=arr("students");String[] names=new String[s.length()];for(int i=0;i<s.length();i++)names[i]=s.optJSONObject(i).optString("display_name");new AlertDialog.Builder(this).setTitle("Choose student").setItems(names,(d,which)->resourceMeta(s.optJSONObject(which))).show();}

    private void resourceMeta(JSONObject student){LinearLayout b=form();EditText title=input("Resource title",false),access=input("Access note / password (optional)",false);Spinner type=new Spinner(this);type.setAdapter(new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,new String[]{"Interactive Lesson","Slides","PDF / Notes","Worksheet","Video","Image","Other"}));CheckBox featured=new CheckBox(this);featured.setText("Feature this resource");for(View v:new View[]{title,type,access,featured})b.addView(v,mb(8));new AlertDialog.Builder(this).setTitle("Upload for "+student.optString("display_name")).setView(b).setNegativeButton("Cancel",null).setPositiveButton("Choose file",(d,w)->{pendingStudent=student;pendingTitle=title.getText().toString();pendingType=type.getSelectedItem().toString();pendingAccess=access.getText().toString();pendingFeatured=featured.isChecked();Intent i=new Intent(Intent.ACTION_OPEN_DOCUMENT);i.addCategory(Intent.CATEGORY_OPENABLE);i.setType("*/*");startActivityForResult(i,FILE_PICK);}).show();}

    @Override protected void onActivityResult(int request,int result,Intent data){super.onActivityResult(request,result,data);if(request!=FILE_PICK||result!=RESULT_OK||data==null||data.getData()==null||pendingStudent==null)return;Uri uri=data.getData();try{byte[] bytes=readBytes(uri,8*1024*1024+1);if(bytes.length>8*1024*1024){toast("File is over 8 MB.");return;}String name=fileName(uri),mime=getContentResolver().getType(uri);if(mime==null)mime="application/octet-stream";JSONObject q=j("action","resourceUpload","studentId",pendingStudent.optString("id"),"title",pendingTitle.isEmpty()?name:pendingTitle,"resourceType",pendingType,"fileName",name,"mimeType",mime,"description","","accessNote",pendingAccess,"completionStatus","Available","featured",pendingFeatured,"base64",android.util.Base64.encodeToString(bytes,android.util.Base64.NO_WRAP));toast("Uploading…");act(q,()->{toast("Resource added");showResources(null,true);});}catch(Exception e){toast(e.getMessage());}pendingStudent=null;}

    private void openResource(JSONObject r){api.post(j("action","resourceDownload","id",r.optString("id")),true,(res,e)->{if(e!=null){toast(e.getMessage());return;}try{startActivity(new Intent(Intent.ACTION_VIEW,Uri.parse(res.optString("signedUrl"))));}catch(Exception x){toast("No app available to open this resource.");}});}

    private View lessonCard(JSONObject l,boolean admin){LinearLayout c=box();LinearLayout top=row();if(admin)top.addView(t(studentName(l.optString("student_id")),12,ink(),true),new LinearLayout.LayoutParams(0,-2,1));top.addView(statusChip(l.optString("status")));c.addView(top);c.addView(t(l.optString("topic"),14,ink(),true),mtb(7,2));c.addView(t(l.optString("lesson_date")+"  •  "+shortTime(l.optString("start_time"))+"  •  "+l.optInt("duration_minutes")+" min",10,muted(),false));c.addView(t(l.optString("subject"),10,orangeStrong(),true),mtb(3,0));return c;}

    private void menu(LinearLayout b,String title,String sub,int accent,View.OnClickListener click){LinearLayout c=row();c.setGravity(Gravity.CENTER_VERTICAL);c.setPadding(dp(14),dp(13),dp(14),dp(13));c.setBackground(round(surface(),16,1,line()));TextView dot=t("•",30,accent,true);c.addView(dot,new LinearLayout.LayoutParams(dp(28),dp(40)));LinearLayout tx=col();tx.addView(t(title,14,ink(),true));tx.addView(t(sub,10,muted(),false));c.addView(tx,new LinearLayout.LayoutParams(0,-2,1));c.addView(t("›",24,muted(),false));c.setOnClickListener(click);b.addView(c,mb(8));}
    private LinearLayout actionTile(String title,String sub,int accent,View.OnClickListener l){LinearLayout c=box();c.addView(t(title,14,accent,true));c.addView(t(sub,11,muted(),false),mtb(3,0));c.setOnClickListener(l);return c;}
    private LinearLayout metric(String label,String value,int accent){LinearLayout c=box();c.addView(t(value,19,accent,true));c.addView(t(label,9,muted(),true));return c;}
    private View progressLine(int a,int total){ProgressBar p=new ProgressBar(this,null,android.R.attr.progressBarStyleHorizontal);p.setMax(Math.max(1,total));p.setProgress(a);p.setProgressTintList(android.content.res.ColorStateList.valueOf(green()));p.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(line()));return p;}
    private TextView statusChip(String s){int c=statusColor(s);return pill(s,soft(c),c);}
    private int statusColor(String s){if("Covered".equals(s)||"Completed".equals(s))return green();if("Next".equals(s)||"Current".equals(s)||"Confirmed".equals(s))return blue();if("Revisit".equals(s))return orangeStrong();if("Resource Ready".equals(s))return purple();if("Declined".equals(s)||"Cancelled".equals(s))return red();return muted();}
    private int soft(int c){if(c==green())return dark?Color.rgb(30,58,40):Color.rgb(239,249,238);if(c==blue())return dark?Color.rgb(25,49,72):Color.rgb(238,246,253);if(c==orangeStrong())return dark?Color.rgb(69,43,34):Color.rgb(255,242,236);if(c==purple())return dark?Color.rgb(49,39,68):Color.rgb(247,242,253);if(c==red())return dark?Color.rgb(70,30,38):Color.rgb(254,240,242);return surface2();}

    private JSONArray arr(String key){JSONArray a=sync.optJSONArray(key);return a==null?new JSONArray():a;}
    private JSONArray topicsFor(String studentId){JSONArray out=new JSONArray();for(JSONObject x:jsonList(arr("topicProgress")))if(studentId.equals(x.optString("student_id")))out.put(x);return out;}
    private JSONObject findStudent(String id){for(JSONObject x:jsonList(arr("students")))if(id.equals(x.optString("id")))return x;return null;}
    private String studentName(String id){JSONObject x=findStudent(id);if(x!=null)return x.optString("display_name");if(sync.optJSONObject("user")!=null&&id.equals(sync.optJSONObject("user").optString("id")))return sync.optJSONObject("user").optString("displayName");return"Student";}
    private int resourceCount(String type,String studentId){int n=0;for(JSONObject r:jsonList(arr("resources")))if(type.equals(r.optString("resource_type"))&&(studentId==null||studentId.equals(r.optString("student_id"))))n++;return n;}
    private int countStatus(JSONArray a,String s){int n=0;for(JSONObject x:jsonList(a))if(s.equals(x.optString("status")))n++;return n;}
    private JSONObject lastMessage(String studentId){for(JSONObject m:jsonList(arr("messages")))if(studentId.equals(m.optString("student_id")))return m;return null;}
    private JSONObject nextLesson(){for(JSONObject l:jsonList(arr("lessons")))if(!"Completed".equals(l.optString("status"))&&!"Cancelled".equals(l.optString("status")))return l;return null;}
    private int pendingCount(){int n=0;for(JSONObject b:jsonList(arr("bookings")))if("Pending".equals(b.optString("status")))n++;return n;}
    private int upcomingCount(){int n=0;for(JSONObject l:jsonList(arr("lessons")))if(!"Completed".equals(l.optString("status"))&&!"Cancelled".equals(l.optString("status")))n++;return n;}
    private int completedValue(){int v=0;for(JSONObject l:jsonList(arr("lessons")))if("Completed".equals(l.optString("status")))v+=l.optInt("duration_minutes")*150/60;return v;}
    private List<JSONObject> jsonList(JSONArray a){List<JSONObject> out=new ArrayList<>();if(a!=null)for(int i=0;i<a.length();i++){JSONObject x=a.optJSONObject(i);if(x!=null)out.add(x);}return out;}

    private void pickDate(Button b,String[] out,Runnable done){Calendar c=Calendar.getInstance();new DatePickerDialog(this,(v,y,m,d)->{out[0]=String.format(Locale.ENGLISH,"%04d-%02d-%02d",y,m+1,d);b.setText(out[0]);if(done!=null)done.run();},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show();}
    private void pickTime(Button b,String[] out){Calendar c=Calendar.getInstance();new TimePickerDialog(this,(v,h,m)->{out[0]=String.format(Locale.ENGLISH,"%02d:%02d",h,m);b.setText(out[0]);},c.get(Calendar.HOUR_OF_DAY),0,true).show();}
    private byte[] readBytes(Uri uri,int max)throws Exception{InputStream in=getContentResolver().openInputStream(uri);ByteArrayOutputStream out=new ByteArrayOutputStream();byte[] buf=new byte[8192];int n,total=0;while((n=in.read(buf))!=-1){total+=n;if(total>max)break;out.write(buf,0,n);}in.close();return out.toByteArray();}
    private String fileName(Uri uri){String n="file";android.database.Cursor c=null;try{c=getContentResolver().query(uri,null,null,null,null);if(c!=null&&c.moveToFirst()){int i=c.getColumnIndex(OpenableColumns.DISPLAY_NAME);if(i>=0)n=c.getString(i);}}finally{if(c!=null)c.close();}return n;}
    private void logout(){token="";sync=new JSONObject();prefs.edit().remove("token").remove("cache").apply();welcome();}

    private LinearLayout page(){LinearLayout b=col();b.setPadding(dp(16),dp(18),dp(16),dp(28));b.setBackgroundColor(bg());return b;}
    private ScrollView scroll(View v){ScrollView s=new ScrollView(this);s.setFillViewport(true);s.setBackgroundColor(bg());s.addView(v);return s;}
    private LinearLayout box(){LinearLayout c=col();c.setPadding(dp(15),dp(15),dp(15),dp(15));c.setBackground(round(surface(),18,1,line()));return c;}
    private LinearLayout empty(String s){LinearLayout c=box();c.addView(t(s,12,muted(),false));return c;}
    private LinearLayout form(){LinearLayout b=col();b.setPadding(dp(3),dp(8),dp(3),0);return b;}
    private LinearLayout row(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.HORIZONTAL);return l;}
    private LinearLayout col(){LinearLayout l=new LinearLayout(this);l.setOrientation(LinearLayout.VERTICAL);return l;}
    private void title(LinearLayout b,String title,String sub){b.addView(t(title,27,ink(),true));b.addView(t(sub,12,muted(),false),mb(15));}
    private void section(LinearLayout b,String s){b.addView(t(s,14,ink(),true),mtb(18,8));}
    private TextView t(String s,float size,int color,boolean bold){TextView v=new TextView(this);v.setText(s);v.setTextSize(size);v.setTextColor(color);v.setLineSpacing(0,1.05f);if(bold)v.setTypeface(Typeface.DEFAULT,Typeface.BOLD);return v;}
    private TextView pill(String s,int bg,int fg){TextView v=t(s,9,fg,true);v.setGravity(Gravity.CENTER);v.setPadding(dp(9),dp(5),dp(9),dp(5));v.setBackground(round(bg,100,0,0));return v;}
    private EditText input(String hint,boolean pass){EditText e=new EditText(this);e.setHint(hint);e.setTextColor(ink());e.setHintTextColor(muted());e.setTextSize(13);e.setPadding(dp(12),dp(11),dp(12),dp(11));e.setBackground(round(surface(),13,1,line()));if(pass)e.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_VARIATION_PASSWORD);return e;}
    private Button primary(String s){return button(s,navy(),Color.WHITE);}
    private Button secondary(String s){Button b=button(s,surface(),ink());b.setBackground(round(surface(),13,1,line()));return b;}
    private Button small(String s){Button b=button(s,surface2(),ink());b.setTextSize(11);b.setBackground(round(surface2(),12,1,line()));return b;}
    private Button button(String s,int bg,int fg){Button b=new Button(this);b.setText(s);b.setAllCaps(false);b.setTextColor(fg);b.setTextSize(12);b.setTypeface(Typeface.DEFAULT,Typeface.BOLD);b.setBackground(round(bg,13,0,0));return b;}
    private GradientDrawable round(int color,int radius,int stroke,int strokeColor){GradientDrawable g=new GradientDrawable();g.setColor(color);g.setCornerRadius(dp(radius));if(stroke>0)g.setStroke(dp(stroke),strokeColor);return g;}
    private GradientDrawable grad(int a,int b,int radius){GradientDrawable g=new GradientDrawable(GradientDrawable.Orientation.TL_BR,new int[]{a,b});g.setCornerRadius(dp(radius));return g;}
    private GradientDrawable circle(int c){GradientDrawable g=new GradientDrawable();g.setShape(GradientDrawable.OVAL);g.setColor(c);return g;}
    private LinearLayout.LayoutParams mb(int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,0,0,dp(b));return p;}
    private LinearLayout.LayoutParams mtb(int t,int b){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-1,-2);p.setMargins(0,dp(t),0,dp(b));return p;}
    private LinearLayout.LayoutParams ml(int l){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(-2,-2);p.setMargins(dp(l),0,0,0);return p;}
    private LinearLayout.LayoutParams w(int weight,int left){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,-2,weight);p.setMargins(dp(left),0,0,0);return p;}
    private LinearLayout.LayoutParams wh(int weight,int left,int h){LinearLayout.LayoutParams p=new LinearLayout.LayoutParams(0,dp(h),weight);p.setMargins(dp(left),0,0,0);return p;}
    private int dp(int x){return Math.round(x*getResources().getDisplayMetrics().density);}
    private int colorFor(int i){int[] c={navy(),orangeStrong(),green(),blue(),purple()};return c[i%c.length];}
    private int parseInt(String s,int d){try{return Integer.parseInt(s.trim());}catch(Exception e){return d;}}
    private int percent(int a,int b){return b<=0?0:Math.round(100f*a/b);}
    private String nz(String s){return s==null||s.isEmpty()||"null".equals(s)?"Not recorded":s;}
    private String initials(String s){if(s==null||s.trim().isEmpty())return"?";String[] p=s.trim().split("\\s+");return p.length==1?p[0].substring(0,1).toUpperCase():(p[0].substring(0,1)+p[p.length-1].substring(0,1)).toUpperCase();}
    private String shortTime(String s){return s==null||s.length()<5?"Time not set":s.substring(0,5);}
    private String messageTime(String s){return s!=null&&s.length()>=16?s.substring(11,16):"";}
    private String greeting(){int h=Calendar.getInstance().get(Calendar.HOUR_OF_DAY);return h<12?"Good morning":h<17?"Good afternoon":"Good evening";}
    private String longDate(){return new java.text.SimpleDateFormat("EEEE, d MMMM",Locale.ENGLISH).format(new Date());}
    private void toast(String s){Toast.makeText(this,s,Toast.LENGTH_SHORT).show();}
}