# LIRC-derived IR codes - provenance

The files in this folder are the original per-brand XML remote definitions this app's
`assets/lirc_ir_codes.json` was converted from, kept here for reproducibility and
attribution.

**Source project:** [LIRC remotes database](https://lirc.sourceforge.net/remotes/) /
[lirc-remotes](https://sourceforge.net/p/lirc-remotes/wiki/Home/) (GPL-licensed), via its
GitHub mirror [probonopd/lirc-remotes](https://github.com/probonopd/lirc-remotes) (branch
`xml`), which stores each remote as Pronto Hex (CCF) plus decoded
Protocol/Device/Subdevice/Function metadata, generated with `lirc2xml` (Dr. Bengt
Mårtensson) and `DecodeIR` (John S. Fine).

**Files fetched (one representative TV remote per brand):**

| Brand     | Source file                                  |
|-----------|-----------------------------------------------|
| Samsung   | `samsung/00008E.xml`                          |
| LG        | `lg/42H3000.xml`                              |
| Sony      | `sony/KV_32LS35E.xml`                         |
| Panasonic | `panasonic/EUR50350.xml`                      |
| JVC       | `jvc/RM-C422.xml`                             |
| Hitachi   | `hitachi/CLE-941.xml`                         |
| Thomson   | `thomson/RC8002N.xml`                         |
| Sharp     | `sharp/1781.xml`                              |
| Philips   | `philips/26PFL5604H.xml`                      |
| Toshiba   | `toshiba/CT-90003.xml`                        |
| Vestel    | `vestel/TV.xml`                               |

**Conversion method:** each `<code>`'s `<ccf>` (Pronto Hex) value was decoded per the
standard Pronto format - `format frequency_code seq1_len seq2_len` header followed by
burst-pair values in units of the carrier period - into an explicit carrier frequency (Hz)
and a raw on/off microsecond pulse array, i.e. exactly what
`android.hardware.ConsumerIrManager.transmit(frequency, pattern)` expects. This is stored
per function as `IrCodeEntity(protocol = RAW, rawPattern = "...", carrierFrequencyHz = ...)`
and bypasses this app's own NEC/Sony/RC5/RC6 heuristic encoder entirely - the signal
transmitted is (up to Pronto's own encoding precision) the same one the original physical
remote produced.

**Scope / honesty notes:**
- Only one sampled remote model per brand was converted, and only for the TV category.
  A different model from the same brand may use a different signal for the same button.
- Not every function on every remote maps cleanly to this app's `RemoteFunction` enum
  (e.g. Vestel's sampled remote has no distinct volume/channel keys under a recognizable
  name), so some functions for some brands still fall back to the placeholder codes in
  `assets/preset_ir_codes.json`.
- This is not a redistribution of LIRC/lirc-remotes as a competing product - it's a
  derived, converted subset embedded for personal device-control use within this app,
  consistent with how the LIRC remotes database is used by other universal-remote
  software. The original project is GPL-licensed; see the links above for full terms.
