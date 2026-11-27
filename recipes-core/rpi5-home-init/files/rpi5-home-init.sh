#!/bin/bash
# rpi5-home-init — runs once per boot until /home is populated.
#
# The /home partition (p4) is a large ext4 volume that persists across OTA
# updates.  On the very first boot it is empty, so we need to re-create every
# user's home directory that useradd put into the rootfs skeleton.
#
# We detect "first boot" by the absence of a lost+found entry (always present
# on a freshly formatted ext4) containing any user directories, i.e. /home/rpi
# does not yet exist.

set -euo pipefail

FLAG=/home/.home-initialised

if [[ -f "${FLAG}" ]]; then
    logger -t rpi5-home-init "Home partition already initialised — skipping."
    exit 0
fi

logger -t rpi5-home-init "First boot: initialising /home partition."

# ── Expand p4 (/home) to fill the rest of the SD card ───────────────────────
# The .wic image uses a 512 MB placeholder; here we grow it to 100% of the
# physical media.  p4 is always the last partition so this is always safe.
HOME_DEV=$(findmnt -n -o SOURCE /home)
DISK=$(lsblk -ndo PKNAME "/dev/${HOME_DEV##*/}" 2>/dev/null || echo "")
if [[ -z "${DISK}" ]]; then
    # fallback: strip trailing partition digit(s)
    DISK=$(echo "${HOME_DEV}" | sed 's/p\?[0-9]\+$//')
fi
PART_NUM=$(cat /sys/class/block/${HOME_DEV##*/}/partition 2>/dev/null || \
           echo "${HOME_DEV}" | grep -o '[0-9]*$')

logger -t rpi5-home-init "Expanding ${HOME_DEV} (disk=${DISK} part=${PART_NUM}) to 100%"
parted -s "${DISK}" resizepart "${PART_NUM}" 100%
resize2fs "${HOME_DEV}"
logger -t rpi5-home-init "Expansion complete."

# Create home directories for every user whose home is under /home and whose
# UID is >= 1000 (regular users, not system accounts).
while IFS=: read -r username _ uid _ _ homedir shell; do
    [[ "${uid}" -lt 1000 ]] && continue
    [[ "${homedir}" != /home/* ]] && continue
    [[ "${shell}" == */nologin ]] || [[ "${shell}" == */false ]] && continue

    if [[ ! -d "${homedir}" ]]; then
        logger -t rpi5-home-init "Creating ${homedir} for ${username} (uid=${uid})"
        install -d -m 0750 -o "${username}" -g "${username}" "${homedir}"
        # Populate from /etc/skel
        cp -a /etc/skel/. "${homedir}/" 2>/dev/null || true
        chown -R "${username}:${username}" "${homedir}"
    fi
done < /etc/passwd

touch "${FLAG}"
logger -t rpi5-home-init "Home partition initialisation complete."
