# SWUpdate OTA for Sequoia Intel x86

This document describes the OTA flow used by Sequoia on Intel x86 machines (including LattePanda-class targets).

## Partition Layout

- `p1` `/boot` - EFI/FAT32
- `p2` `rootfs-a` - ext4 (active slot at factory flash)
- `p3` `rootfs-b` - ext4 (inactive slot, OTA target)
- `p4` `/home` - ext4 (persistent data/home)

The update scripts identify slots by filesystem label (`rootfs-a`/`rootfs-b`), making the flow independent of device node naming (`/dev/sda*`, `/dev/nvme*`, etc.).

## Update Flow

1. `pre-install.sh` reads active root label and resolves inactive device label.
2. SWUpdate writes `rootfs.ext4.gz` to `/dev/swupdate-inactive` using the `raw` handler.
3. `post-install.sh` updates bootloader config under `/boot` to `root=LABEL=rootfs-a|b`.
4. `swupdate.cfg` triggers reboot via `postupdatecmd`.

## Packaging Files

- `sw-description`
- `pre-install.sh`
- `post-install.sh`
- `rootfs.ext4.gz`

## Delivery

- Web UI: `http://<device-ip>:8080`
- CLI: `swupdate-client -v <device-ip> sequoia-intel-<version>.swu`

## Troubleshooting

- Verify labels:
  - `blkid | grep -E 'rootfs-a|rootfs-b'`
- Verify active root:
  - `findmnt -n -o SOURCE /`
  - `findmnt -n -o SOURCE / | xargs -r blkid -o value -s LABEL`
- Verify boot entries were updated:
  - `grep -R "root=" /boot/loader/entries /boot/grub /boot/grub2 2>/dev/null`
