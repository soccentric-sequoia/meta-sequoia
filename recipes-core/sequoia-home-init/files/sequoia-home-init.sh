#!/bin/bash
# sequoia-home-init - runs once per boot until /home is populated.

set -euo pipefail

FLAG=/home/.home-initialised

if [[ -f "${FLAG}" ]]; then
    logger -t sequoia-home-init "Home partition already initialised; skipping."
    exit 0
fi

logger -t sequoia-home-init "First boot: initialising /home partition."

# Grow the /home partition to use remaining disk space. This assumes /home is
# the last partition in the WIC layout.
HOME_DEV=$(findmnt -n -o SOURCE /home)
DISK=$(lsblk -ndo PKNAME "/dev/${HOME_DEV##*/}" 2>/dev/null || echo "")
if [[ -z "${DISK}" ]]; then
    DISK=$(echo "${HOME_DEV}" | sed 's/p\?[0-9]\+$//')
fi
PART_NUM=$(cat /sys/class/block/${HOME_DEV##*/}/partition 2>/dev/null || \
           echo "${HOME_DEV}" | grep -o '[0-9]*$')

logger -t sequoia-home-init "Expanding ${HOME_DEV} (disk=${DISK} part=${PART_NUM}) to 100%"
parted -s "/dev/${DISK}" resizepart "${PART_NUM}" 100%
resize2fs "${HOME_DEV}"
logger -t sequoia-home-init "Expansion complete."

while IFS=: read -r username _ uid _ _ homedir shell; do
    [[ "${uid}" -lt 1000 ]] && continue
    [[ "${homedir}" != /home/* ]] && continue
    [[ "${shell}" == */nologin ]] || [[ "${shell}" == */false ]] && continue

    if [[ ! -d "${homedir}" ]]; then
        logger -t sequoia-home-init "Creating ${homedir} for ${username} (uid=${uid})"
        install -d -m 0750 -o "${username}" -g "${username}" "${homedir}"
        cp -a /etc/skel/. "${homedir}/" 2>/dev/null || true
        chown -R "${username}:${username}" "${homedir}"
    fi
done < /etc/passwd

touch "${FLAG}"
logger -t sequoia-home-init "Home partition initialisation complete."
