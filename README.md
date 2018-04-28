# lq-uploader

This is a custom uploader that uploads using scp, through a jump host to a final destination.

## Installation

Compile with

```
lein uberjar
```

and get the jar from `target/uberjar/lq-uploader-0.1.0-SNAPSHOT-standalone.jar`

## Directory structure

The configuration must be included in the present working directory, in a file called `config.edn`.

See `sample-config.edn` for an example.

## Usage

Double click `lq-uploader-0.1.0-SNAPSHOT-standalone.jar` or `lq-uploader.jar` and
drag and drop images to upload.

### How to add a cover to the slider

1. On the sidebar, click "Extensions" then when it unrolls, click "Extensions" again.
2. In the drop-down menu for choosing extension type, select "Modules (30)"
3. In the "TG ThemeGlobal Lite Revolution Slider" row, click the pencil button
4. Click "Existing modules"
5. Click "Edit"

You can click through the slides and enable and disable as necessary using the on/off slider.

### Adding slides

Add slides using the "Add Slide" button.

1. Click the on/off slider to enable this slide
2. Add a picture. Select it from the "covers" folder.
3. For the "Transition slider effect", use "slidehorizontal"
4. Remember to click "Save" to save all your changes.

## License

Copyright © 2018 ackerleytng

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
