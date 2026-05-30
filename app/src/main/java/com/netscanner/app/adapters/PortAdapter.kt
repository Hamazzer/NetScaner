package com.netscanner.app.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.netscanner.app.R
import com.netscanner.app.models.Port

class PortAdapter(
    private val ports: List<Port>
) : RecyclerView.Adapter<PortAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val tvPort: TextView = view.findViewById(R.id.tvPort)
        val tvService: TextView = view.findViewById(R.id.tvService)
        val tvProtocol: TextView = view.findViewById(R.id.tvProtocol)
        val tvVersion: TextView = view.findViewById(R.id.tvVersion)
        val tvState: TextView = view.findViewById(R.id.tvState)
        val tvRisk: TextView = view.findViewById(R.id.tvRisk)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VH(LayoutInflater.from(parent.context).inflate(R.layout.item_port, parent, false))

    override fun getItemCount() = ports.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val port = ports[position]
        holder.tvPort.text = port.number.toString()
        holder.tvService.text = port.service.uppercase()
        holder.tvProtocol.text = port.protocol.uppercase()
        holder.tvVersion.text = if (port.version.isNotBlank()) port.version else "—"
        holder.tvState.text = port.state.uppercase()
        val risk = getRisk(port.number)
        holder.tvRisk.text = risk.first
        holder.tvRisk.setTextColor(
            holder.itemView.context.getColor(risk.second)
        )
    }

    private fun getRisk(port: Int): Pair<String, Int> {
        return when (port) {
            21 -> "ВЫСОКИЙ" to R.color.red_accent
            23 -> "КРИТИЧЕСКИЙ" to R.color.red_accent
            3389 -> "ВЫСОКИЙ" to R.color.red_accent
            5900 -> "ВЫСОКИЙ" to R.color.red_accent
            1433, 3306, 5432, 27017 -> "СРЕДНИЙ" to R.color.orange_accent
            80, 8080 -> "НИЗКИЙ" to R.color.green_accent
            443, 8443 -> "НИЗКИЙ" to R.color.green_accent
            22 -> "СРЕДНИЙ" to R.color.orange_accent
            else -> "ИНФО" to R.color.text_secondary
        }
    }
}
