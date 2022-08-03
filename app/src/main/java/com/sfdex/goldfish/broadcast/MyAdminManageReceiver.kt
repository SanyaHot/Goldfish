package com.sfdex.goldfish.broadcast

import android.app.admin.*
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

//设备管理器
class MyAdminManageReceiver : DeviceAdminReceiver() {
    override fun onNetworkLogsAvailable(
        context: Context, intent: Intent, batchToken: Long, networkLogsCount: Int
    ) {

        val dpm = getManager(context)
        var logs: List<NetworkEvent>? = null

        // Fetch the batch of logs with the batch token from the callback's arguments.
        try {
            logs = dpm.retrieveNetworkLogs(getWho(context), batchToken)
        } catch (e: SecurityException) {
            // Perhaps an unaffiliated user - handle the exception ...
            e.printStackTrace()
        }

        // Process any logs ...

        // Here, logs might be null. We can't fix because either the token doesn't match
        // the current batch or network logging was deactivated.
        // Confirm with isNetworkLoggingEnabled().

        logs?.forEach {
            // For this example, report the DNS hosts and discard all other data.
            // Because we use the event ID, this example requires API level 28.
            if (it is DnsEvent) {
                val dnsLog = buildString {
                    append("id: ")
                    append(it.id)
                    append(", time: ")
                    append(it.timestamp)
                    append(", hostname: ")
                    append(it.hostname)
                }
                Log.d(TAG, "dnsLog: $dnsLog")
                //reportDnsHostToServer(it.hostname, it.getTimestamp(), it.getId())
            }
            if (it is ConnectEvent) {
                val connectLog = buildString {
                    append("id: ")
                    append(it.id)
                    append(", time: ")
                    append(it.timestamp)
                    append(", port: ")
                    append(it.port)
                    append(", packageName: ")
                    append(it.packageName)
                    append(", hostName: ")
                    append(it.inetAddress.hostName)
                    append(", hostAddress: ")
                    append(it.inetAddress.hostAddress)
                    append(", canonicalHostName: ")
                    append(it.inetAddress.canonicalHostName)
                }
                Log.d(TAG, "connect: $connectLog")
            }
        }
    }
}

private const val TAG = "MyAdminManageReceiver"