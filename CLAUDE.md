# Sequoia-OS — Yocto embedded Linux for Intel x86 (LattePanda)

## Overview

Sequoia-OS is the Yocto/OpenEmbedded build system for the **Sequoia**
platform — an **Intel x86** SoM + platform-I/O south-bridge. Reference board:
**LattePanda**. Built with [kas](https://kas.readthedocs.io/) + Docker
(`genericx86-64` machine, EFI/GRUB boot), with A/B OTA updates via
[swupdate](https://sbabic.github.io/swupdate/).

**Architecture note:** unlike the other projects, **Sequoia-OS is itself the
`meta-sequoia` Yocto layer** — `conf/layer.conf` lives at the repo root, so
`kas/repos/meta-sequoia.yaml` points at `path: .`.

## Quick Start

```bash
make kas-image            # Build the KAS Docker image (once)
make image sequoia        # Build the Yocto image (genericx86-64 / LattePanda)
```

## Build Targets

```
make image  sequoia         Build Yocto SD card image
make build  sequoia         Alias for image
make rebuild-image sequoia  cleansstate image recipe, then rebuild
make sdk    sequoia         Cross-toolchain SDK installer → deploy/sdk/
make esdk   sequoia         Extensible SDK installer     → deploy/esdk/
make shell  sequoia         Interactive kas shell
make swu                      Build .swu OTA package
make flash  sequoia DEVICE=/dev/sdX   Flash image to SD card
```

## Boot Flow (Intel x86 / LattePanda)

```
UEFI → GRUB (grub.cfg, A/B-aware) → Linux (Image + initrd) → systemd
     → Sequoia-HAL, services
```

## A/B OTA Layout

Partition table (`wic/sequoia-intel.wks`, EFI GPT):
```
  p1  /boot   EFI FAT32   Image, grub.cfg
  p2  /       rootfs-a (active initially)
  p3  standby rootfs-b (OTA target)
  p4  /home   persistent, grown on first boot by sequoia-home-init
```

SWUpdate scripts switch slots by filesystem label (`rootfs-a`/`rootfs-b`).
`make swu` builds an `.swu` package; signing via
`scripts/generate-swu-keys.sh` (RSA-4096).

## Layer Structure

| Layer               | Source                        | Purpose                              |
|---------------------|-------------------------------|--------------------------------------|
| `poky`              | poky (scarthgap)              | OE core + genericx86-64 machine     |
| `meta-oe`           | meta-openembedded (scarthgap) | Package set                          |
| `meta-swupdate`     | sbabic/meta-swupdate (scarthgap) | swupdate OTA framework           |
| `meta-sequoia`      | SiliconCentric/meta-sequoia (this repo root) | Sequoia image, HAL, swu config |

## Repo Notes

- `build/`, `downloads/`, `sstate-cache/`, `layers/` are gitignored
  bind-mounted host dirs.
- This repo **is** the `meta-sequoia` layer (root `conf/layer.conf`); upstream
  layers are cloned by kas into `layers/` and must never be hand-edited.
- Legacy Raspberry Pi 5 recipes (`rpi5-*`) are retained for reference only;
  the active image path uses the Sequoia recipes.
