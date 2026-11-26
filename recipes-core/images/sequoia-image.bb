SUMMARY = "Sequoia OS — production image for Intel x86 (LattePanda)"
DESCRIPTION = "Full image with A/B OTA (SWUpdate), gRPC/Protobuf middleware, \
hardware interface tooling, and connectivity."
HOMEPAGE = "https://github.com/soccentric-sequoia/Sequoia-OS"
LICENSE = "MIT"

# Base on core-image-full-cmdline (provides bash, sshd-openssh, package
# management and the base IMAGE_INSTALL framework).  core-image-full-cmdline
# is an *image recipe*, not a bbclass, so it must be pulled in with `require`
# (not `inherit`).  The previous split between this recipe (missing) and a
# core-image-full-cmdline.bbappend left the kas target `sequoia-image`
# without a provider; the canonical fix (mirroring Acadia/Orion) is a single
# recipe carrying all the image deltas.
require recipes-extended/images/core-image-full-cmdline.bb
IMAGE_FEATURES += "ssh-server-openssh"

# OTA / swupdate — A/B rootfs slot switching, web UI on :8080
IMAGE_INSTALL:append = " \
    swupdate \
    swupdate-www \
    sequoia-swupdate-config \
"

# Persistent /home partition init — creates /home/rpi on first boot after
# the empty ext4 home partition (p4) is mounted over /home.
IMAGE_INSTALL:append = " sequoia-home-init"

# Connectivity — Ethernet DHCP, WiFi (wpa_supplicant), Bluetooth
# WiFi/BT packages are included for LattePanda variants with wireless modules.
IMAGE_INSTALL:append = " \
    sequoia-connectivity \
    wpa-supplicant \
    iw \
    rfkill \
    bluez5 \
    bluez5-dev \
"

# gRPC + Protocol Buffers (C++ runtime, compiler plugin, and Python bindings)
IMAGE_INSTALL:append = " \
    protobuf \
    protobuf-compiler \
    protobuf-lite \
    grpc \
    python3-grpcio \
    python3-protobuf \
    sudo \
"

# --- Hardware interface tools ------------------------------------------------

# I2C
IMAGE_INSTALL:append = " \
    i2c-tools \
    python3-smbus \
    python3-smbus2 \
"

# SPI
IMAGE_INSTALL:append = " \
    spitools \
    python3-spidev \
"

# GPIO
IMAGE_INSTALL:append = " \
    libgpiod \
    libgpiod-tools \
    python3-gpiod \
"

# UART / serial
IMAGE_INSTALL:append = " \
    picocom \
    minicom \
    python3-pyserial \
"

# USB
IMAGE_INSTALL:append = " \
    usbutils \
"

# PCI / NVMe / SCSI / storage
IMAGE_INSTALL:append = " \
    pciutils \
    nvme-cli \
    sg3-utils \
    hdparm \
    mtd-utils \
"

# CAN bus
IMAGE_INSTALL:append = " \
    can-utils \
"

# System / hardware inspection
IMAGE_INSTALL:append = " \
    lshw \
    ethtool \
    devmem2 \
    cpupower \
    watchdog \
    sysstat \
    iotop \
    strace \
    v4l-utils \
    evtest \
    python3-periphery \
"

# Intel x86 machine defaults use an EFI-based A/B WIC layout.
WKS_FILE:genericx86-64 = "sequoia-intel.wks"
WKS_FILE:genericx86 = "sequoia-intel.wks"
WKS_FILE:intel-corei7-64 = "sequoia-intel.wks"
WKS_FILE:lattepanda = "sequoia-intel.wks"
WKS_FILE:lattepanda-64 = "sequoia-intel.wks"

# Operator account 'rpi' with password 'rpi' and sudo access
inherit extrausers
EXTRA_USERS_PARAMS = "\
    useradd -m -s /bin/bash -G sudo rpi; \
    usermod -p '\$6\$8.kfghMSYkTHsFqs\$fKQIEp.xMjitUYt9yXjEZBUvKqUOQvxekn5.9hWP9ewiLFSI0LQi5OpaDNfSqh4OU.tzN7co7olMAYg53/kCP0' rpi; \
"
