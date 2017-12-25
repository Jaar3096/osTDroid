package id.web.devnull.ostdroid;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.util.Log;
import android.content.Intent;
import android.content.Context;

import java.util.List;
import id.web.devnull.ostdroid.scp.*;

public class TAdapter extends RecyclerView.Adapter<TAdapter.VHolder>
{
        private static final String TAG = "osTDroid";
        public List<Ticket> data = null;
        private Context ctx;

        public TAdapter(Context ctx)
        {
                this.ctx = ctx;
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
}
