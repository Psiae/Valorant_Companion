package dev.flammky.valorantcompanion.time.truetime.impl

import com.instacart.truetime.TrueTimeEventListener
import com.instacart.truetime.time.TrueTimeParameters
import java.net.InetAddress
import java.util.*

interface NoOpTrueTimeEventListenerInterface : TrueTimeEventListener {


    override fun sntpRequest(address: InetAddress) { }

    override fun sntpRequestFailed(address: InetAddress, e: Exception) {}

    override fun sntpRequestSuccessful(address: InetAddress) { }

    override fun returningDeviceTime() {  }

    override fun returningTrueTime(trueTime: Date) {  }

    override fun storingTrueTime(ntpResult: LongArray) {  }

    override fun initialize(params: TrueTimeParameters) {}

    override fun initializeFailed(e: Exception) { }

    override fun initializeSuccess(ntpResult: LongArray) {  }

    override fun lastSntpRequestAttempt(ipHost: InetAddress) { }

    override fun nextInitializeIn(delayInMillis: Long) { }

    override fun resolvedNtpHostToIPs(ntpHost: String, ipList: List<InetAddress>) { }

    override fun sntpRequestFailed(e: Exception) { }

    override fun syncDispatcherException(t: Throwable) { }
}