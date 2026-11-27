# Sequoia-OS Yocto Layer

This layer provides Sequoia-specific image customizations, OTA integration, connectivity defaults, and first-boot initialization for Intel x86 targets.

## Primary Target

- Platform: Sequoia Intel x86
- Reference board: LattePanda
- Yocto series: scarthgap

## Key Components

- `recipes-core/images/core-image-full-cmdline.bbappend`
  - Adds Sequoia packages and hardware tooling.
  - Sets Intel x86 WIC file overrides to `sequoia-intel.wks`.
- `recipes-swupdate/sequoia-swupdate-config/`
  - SWUpdate runtime configuration and A/B helper scripts.
- `recipes-core/sequoia-home-init/`
  - First-boot `/home` expansion and user-home initialization.
- `recipes-connectivity/sequoia-connectivity/`
  - systemd-networkd defaults for Ethernet/WiFi VLAN and resolver preset.
- `wic/sequoia-intel.wks`
  - EFI GPT image layout with A/B rootfs and persistent `/home`.

## A/B Layout (Sequoia Intel)

- `p1` `/boot` (EFI FAT32)
- `p2` `/` (`rootfs-a`, active initially)
- `p3` standby (`rootfs-b`, OTA target)
- `p4` `/home` (persistent, grown on first boot)

SWUpdate scripts switch slots using filesystem labels (`rootfs-a`/`rootfs-b`) instead of hardcoded block device names.

## Notes

Legacy Raspberry Pi 5 recipes are still present in this repository for reference, but the active image path now uses Sequoia Intel recipes.
