
## Updating `licenses.xml`

The licenses are held in `redhat/licenses/licenses.xml`.

You can update these from the license info in the POMs for the
transitive compile-scopes dependenencies by running

    mvn -P update-licences clean package -DskipTests

But doing this is not sufficient (see https://docs.engineering.redhat.com/display/JPC/Product+Licensing+Information#ProductLicensingInformation-Requirements).
So having done this you will likely need to edit the licenses.xml file by hand to ensure that it is correct.

## Rendering `licenses.html`

The `licenses.xsl` file will render `licenses.html`, you can execute

    mvn -P render-licenses clean package -DskipTests

to do this. Note that `licenses.xsl` should not be changed.

You should then verify `licenses.html`, and in particular that the links to the local licenses are all working.

