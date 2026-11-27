SUMMARY = "RPi5 connectivity — DHCP on Ethernet, WiFi (wpa_supplicant) and Bluetooth"
LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

SRC_URI = " \
    file://10-eth.network \
    file://20-wlan.network \
    file://10-eth-vlan10.netdev \
    file://20-wlan-vlan10.netdev \
    file://11-eth-vlan10.network \
    file://21-wlan-vlan10.network \
    file://wpa_supplicant-wlan0.conf \
    file://80-rpi5-connectivity.preset \
"

# wpa-supplicant and bluez5 must be present for the preset to enable them
RDEPENDS:${PN} = "wpa-supplicant bluez5"

do_install() {
    # ── systemd-networkd network/netdev files ─────────────────────────────────
    install -d ${D}${sysconfdir}/systemd/network
    install -m 0644 ${WORKDIR}/10-eth.network \
                    ${D}${sysconfdir}/systemd/network/10-eth.network
    install -m 0644 ${WORKDIR}/20-wlan.network \
                    ${D}${sysconfdir}/systemd/network/20-wlan.network
    install -m 0644 ${WORKDIR}/10-eth-vlan10.netdev \
                    ${D}${sysconfdir}/systemd/network/10-eth-vlan10.netdev
    install -m 0644 ${WORKDIR}/20-wlan-vlan10.netdev \
                    ${D}${sysconfdir}/systemd/network/20-wlan-vlan10.netdev
    install -m 0644 ${WORKDIR}/11-eth-vlan10.network \
                    ${D}${sysconfdir}/systemd/network/11-eth-vlan10.network
    install -m 0644 ${WORKDIR}/21-wlan-vlan10.network \
                    ${D}${sysconfdir}/systemd/network/21-wlan-vlan10.network

    # ── wpa_supplicant stub config (fill in SSID/PSK on target) ───────────────
    install -d ${D}${sysconfdir}/wpa_supplicant
    install -m 0600 ${WORKDIR}/wpa_supplicant-wlan0.conf \
                    ${D}${sysconfdir}/wpa_supplicant/wpa_supplicant-wlan0.conf

    # ── systemd preset — enable networkd, resolved, wpa_supplicant, BT ───────
    install -d ${D}${systemd_unitdir}/system-preset
    install -m 0644 ${WORKDIR}/80-rpi5-connectivity.preset \
                    ${D}${systemd_unitdir}/system-preset/80-rpi5-connectivity.preset

    # ── Point /etc/resolv.conf at systemd-resolved stub ───────────────────────
    install -d ${D}${sysconfdir}
    ln -sf ../run/systemd/resolve/stub-resolv.conf \
           ${D}${sysconfdir}/resolv.conf
}

FILES:${PN} += " \
    ${sysconfdir}/systemd/network/ \
    ${sysconfdir}/wpa_supplicant/ \
    ${systemd_unitdir}/system-preset/ \
    ${sysconfdir}/resolv.conf \
"
