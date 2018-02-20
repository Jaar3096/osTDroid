package id.web.devnull.ostdroid;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Filter;
import android.widget.Filterable;
import android.util.Log;
import android.content.Intent;
import android.content.Context;

import java.util.List;
import java.util.LinkedList;
import java.lang.StringBuilder;
import id.web.devnull.ostdroid.scp.*;

public class    TAdapter
extends         RecyclerView.Adapter<TAdapter.VHolder>
implements      Filterable
{
        private static final String TAG = "osTDroid";
        public List<Ticket> data = null;
        private List<Ticket> data_temp = null;
        private Context ctx;

        public TAdapter(Context ctx, List<Ticket> tlist)
        {
                this.ctx = ctx;
                this.data = tlist;
        }

        public class VHolder extends RecyclerView.ViewHolder
        {
                public TextView tid;
                public TextView ttopic;
                public TextView tdate;
                public TextView ttitle;

                public VHolder(View v) {
                        super(v);
                        tid     = v.findViewById(R.id.tid);
                        ttopic  = v.findViewById(R.id.ttopic);
                        tdate   = v.findViewById(R.id.tdate);
                        ttitle  = v.findViewById(R.id.ttitle);
                }
        }

        public void write_index(int ticket_state) {
                try {
                        scp.write_index(this.data, ticket_state);
                } catch (Exception e) {
                        Log.e(TAG, "Error writing index", e);
                }
        }

        @Override
        public VHolder onCreateViewHolder(ViewGroup vg, int vtype)
        {
                View v = LayoutInflater.from(vg.getContext())
                         .inflate(R.layout.tlist, vg, false);

                final VHolder vh = new VHolder(v);
                v.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                                int pos = vh.getAdapterPosition();
                                Intent i = new Intent(ctx, TView.class);
                                i.putExtra("tid", data.get(pos).tid);
                                ctx.startActivity(i);
                        }
                });

                return vh;
        }

        @Override
        public void onBindViewHolder(VHolder vh, int pos)
        {
                vh.tid.setText(data.get(pos).tid);
                vh.ttopic.setText(data.get(pos).topic);
                vh.tdate.setText(data.get(pos).date);
                vh.ttitle.setText(data.get(pos).title);
        }

        @Override
        public int getItemCount()
        {
                if (data == null)
                        return 0;
                else    return data.size();
        }

        @Override
        public Filter getFilter() {
                if (data_temp == null)
                        data_temp = data;

                return new Filter()
                {
                        @Override
                        protected FilterResults performFiltering (CharSequence c) {
                                String q = c.toString().toLowerCase();
                                if (q.isEmpty() || q == null)
                                        return null;

                                List<Ticket> data_filtered = new LinkedList<Ticket>();
                                for (Ticket data_item : data_temp) {
                                        StringBuilder sb = new StringBuilder();
                                        sb.append(data_item.tid);
                                        sb.append(data_item.title);
                                        sb.append(data_item.user);
                                        sb.append(data_item.user_email);
                                        sb.append(data_item.dprt);
                                        sb.append(data_item.topic);
                                        sb.append(data_item.source);
                                        sb.append(data_item.asgn);
                                        sb.append(data_item.agent_asgn);
                                        sb.append(data_item.team_asgn);

                                        if (sb.toString().toLowerCase().contains(q))
                                                data_filtered.add(data_item);
                                }

                                FilterResults fr = new FilterResults();
                                fr.values = data_filtered;

                                return fr;
                        }

                        @Override
                        protected void publishResults (CharSequence c, FilterResults f) {
                                if (f != null)
                                        data = (List<Ticket>) f.values;
                                else    data = data_temp;
                                notifyDataSetChanged();
                        }
                };
        }
}
