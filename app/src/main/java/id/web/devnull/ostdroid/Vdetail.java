package id.web.devnull.ostdroid;

import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.TextView;
import android.view.ViewGroup;
import android.view.LayoutInflater;
import android.os.Bundle;
import android.os.AsyncTask;
import android.util.Log;
import android.content.Context;

import id.web.devnull.ostdroid.scp.*;

public class Vdetail extends Fragment
{
        public Ticket ticket = null;

        @Override
        public View onCreateView(LayoutInflater li, ViewGroup vg, Bundle savedInstanceState) {
               ViewGroup v = (ViewGroup) li.inflate(R.layout.vdetail, vg, false);
               TextView vid     = v.findViewById(R.id.vid);
               TextView vuser   = v.findViewById(R.id.vuser);
               TextView vdate   = v.findViewById(R.id.vdate);
               TextView vdue_date = v.findViewById(R.id.vdue_date);
               TextView vmdfy   = v.findViewById(R.id.vmdfy);
               TextView vdprt   = v.findViewById(R.id.vdprt);
               TextView vasgn   = v.findViewById(R.id.vasgn);
               TextView vstatus = v.findViewById(R.id.vstatus);
               TextView vprio   = v.findViewById(R.id.vprio);

               vid.setText("[" + this.ticket.tid + "] " + this.ticket.title);
               vuser.setText("From: " + this.ticket.user + 
                               " <"+this.ticket.user_email+">");
               vdate.setText(this.ticket.date);
               vdue_date.setText(this.ticket.due_date);
               vmdfy.setText(this.ticket.mdfy);
               vdprt.setText(this.ticket.dprt);
               vasgn.setText(this.ticket.asgn);
               vstatus.setText(this.ticket.status);
               vprio.setText(this.ticket.prio);

               return v;
        }
}
