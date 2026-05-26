SUMMARY     = "Sequoia OS image for Intel x86 (LattePanda)"
DESCRIPTION = "Full-featured Sequoia OS image: systemd init, SWUpdate A/B OTA, \
GRUB EFI boot, Intel WiFi + Ethernet, gRPC/protobuf runtime, hardware interface \
tools (I2C, SPI, GPIO, CAN, UART, PCIe/NVMe), and a persistent /home partition."

LICENSE = "MIT"
LIC_FILES_CHKSUM = "file://${COMMON_LICENSE_DIR}/MIT;md5=0835ade698e0bcf8506ecda2f7b4f302"

# Start from core-image-full-cmdline which gives us a complete CLI system.
inherit core-image

IMAGE_FEATURES += "ssh-server-openssh"

# ── OTA via SWUpdate ──────────────────────────────────────────────────────────
IMAGE_INSTALL:append = " \
    swupdate \
    swupdate-www \
    sequoia-swupdate-config \
"

# ── Persistent /home partition init ──────────────────────────────────────────
IMAGE_INSTALL:append = " sequoia-home-init"

# ── Connectivity — Ethernet DHCP, WiFi (wpa_supplicant), Bluetooth ────────────
IMAGE_INSTALL:append = " \
    sequoia-connectivity \
    wpa-supplicant \
    iw \
    rfkill \
    bluez5 \
    bluez5-dev \
"

# ── Intel WiFi firmware (Intel AX200/AX201/AX210 and related chips) ───────────
# linux-firmware-iwlwifi covers most Intel wireless adapters used on LattePanda.
IMAGE_INSTALL:append = " \
    linux-firmware-iwlwifi \
"

# ── GRUB EFI bootloader (brought in by grub-efi via EFI_PROVIDER but listed
#    explicitly for e2label / grub-editenv tooling in userspace) ──────────────
IMAGE_INSTALL:append = " \
    grub-efi \
    e2fsprogs \
"

# ── gRPC + Protocol Buffers ───────────────────────────────────────────────────
IMAGE_INSTALL:append = " \
    protobuf \
    protobuf-compiler \
    protobuf-lite \
    grpc \
    python3-grpcio \
    python3-protobuf \
    sudo \
"

# ── Hardware interface tools ──────────────────────────────────────────────────

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

# GPIO (via Linux GPIO character device — works on Intel via ACPI/PCH GPIO)
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

# ── Add local user 'sequoia' with password 'sequoia' and sudo access ──────────
# Password hash: echo -n "sequoia" | openssl passwd -6 -stdin
inherit extrausers

EXTRA_USERS_PARAMS = "\
    useradd -m -s /bin/bash -G sudo sequoia; \
    usermod -p '\$6\$rounds=10000\$uSQH8O3uoYhR\$7VFNEH.uPQhJ1L8UHX4B/qoSZ3Gv1fDhABXwV4HhbVJl/iNQ3Rl3gYa6.q.qMF3TnIPQ4MqGlnHKJRSnCJCe/' sequoia; \
"
