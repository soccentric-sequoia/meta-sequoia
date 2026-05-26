#!/bin/bash
# expand-rootfs.sh — Grow root partition and filesystem to fill the storage device.
# Runs once on first boot via systemd.  A flag file prevents repeated runs.
# Handles NVMe (nvme0n1p2), SATA/USB (sda2), and eMMC (mmcblk0p2).

set -euo pipefail

FLAG="/var/lib/expand-rootfs-done"

if [ -f "$FLAG" ]; then
    echo "expand-rootfs: already done, skipping."
    exit 0
fi

# ── Resolve root block device ─────────────────────────────────────────────────
ROOT_DEV=$(findmnt -n -o SOURCE /)
ROOT_DEV_NAME="${ROOT_DEV#/dev/}"

if [[ "$ROOT_DEV_NAME" =~ ^(nvme[0-9]+n[0-9]+)p([0-9]+)$ ]]; then
    # NVMe: nvme0n1p2 → disk=nvme0n1, part=2
    DISK_NAME="${BASH_REMATCH[1]}"
    PART_NUM="${BASH_REMATCH[2]}"
elif [[ "$ROOT_DEV_NAME" =~ ^(mmcblk[0-9]+)p([0-9]+)$ ]]; then
    # eMMC/SD: mmcblk0p2 → disk=mmcblk0, part=2
    DISK_NAME="${BASH_REMATCH[1]}"
    PART_NUM="${BASH_REMATCH[2]}"
elif [[ "$ROOT_DEV_NAME" =~ ^([a-z]+)([0-9]+)$ ]]; then
    # SATA/USB: sda2 → disk=sda, part=2
    DISK_NAME="${BASH_REMATCH[1]}"
    PART_NUM="${BASH_REMATCH[2]}"
else
    echo "expand-rootfs: cannot parse device '$ROOT_DEV', aborting." >&2
    exit 1
fi

DISK_DEV="/dev/$DISK_NAME"

echo "expand-rootfs: resizing partition $PART_NUM on $DISK_DEV ..."
parted -s "$DISK_DEV" resizepart "$PART_NUM" 100%

# Inform the kernel of the updated partition table
partprobe "$DISK_DEV" 2>/dev/null || true
udevadm settle

echo "expand-rootfs: growing filesystem on $ROOT_DEV ..."
resize2fs "$ROOT_DEV"

mkdir -p "$(dirname "$FLAG")"
touch "$FLAG"

NEW_SIZE=$(df -h / | awk 'NR==2{print $2}')
echo "expand-rootfs: done — root filesystem is now $NEW_SIZE."
