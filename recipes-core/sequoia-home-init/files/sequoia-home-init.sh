#!/bin/bash
# sequoia-home-init — runs once per boot until /home is populated.
#
# The /home partition (p4) is a large ext4 volume that persists across OTA
# updates.  On the very first boot it is empty, so we need to re-create every
# user's home directory that useradd put into the rootfs skeleton.
#
# We detect "first boot" by the absence of a flag file /home/.home-initialised.

set -euo pipefail

FLAG=/home/.home-initialised

if [[ -f "${FLAG}" ]]; then
    logger -t sequoia-home-init "Home partition already initialised — skipping."
    exit 0
fi

logger -t sequoia-home-init "First boot: initialising /home partition."

# ── Expand p4 (/home) to fill the rest of the storage device ─────────────────
# The .wic image uses a 512 MB placeholder; here we grow it to 100% of the
# physical media.  p4 is always the last partition so this is always safe.
HOME_DEV=$(findmnt -n -o SOURCE /home)
DISK=$(lsblk -ndo PKNAME "/dev/${HOME_DEV##*/}" 2>/dev/null || echo "")
if [[ -z "${DISK}" ]]; then
    # Fallback: strip trailing partition digit(s) for NVMe (nvme0n1p4) and SATA (sda4)
    DISK=$(echo "${HOME_DEV}" | sed 's/p\?[0-9]\+$//')
fi
PART_NUM=$(cat /sys/class/block/${HOME_DEV##*/}/partition 2>/dev/null || \
           echo "${HOME_DEV}" | grep -o '[0-9]*$')

logger -t sequoia-home-init "Expanding ${HOME_DEV} (disk=${DISK} part=${PART_NUM}) to 100%"
parted -s "/dev/${DISK}" resizepart "${PART_NUM}" 100%
resize2fs "/dev/${HOME_DEV}"
logger -t sequoia-home-init "Expansion complete."

# ── Create home directories for every user with UID >= 1000 ──────────────────
while IFS=: read -r username _ uid _ _ homedir shell; do
    [[ "${uid}" -lt 1000 ]] && continue
    [[ "${homedir}" != /home/* ]] && continue
    [[ "${shell}" == */nologin ]] && continue
    [[ "${shell}" == */false ]] && continue

    if [[ ! -d "${homedir}" ]]; then
        logger -t sequoia-home-init "Creating ${homedir} for ${username} (uid=${uid})"
        install -d -m 0750 -o "${username}" -g "${username}" "${homedir}"
        # Populate from /etc/skel
        cp -a /etc/skel/. "${homedir}/" 2>/dev/null || true
        chown -R "${username}:${username}" "${homedir}"
    fi
done < /etc/passwd

touch "${FLAG}"
logger -t sequoia-home-init "Home partition initialisation complete."
