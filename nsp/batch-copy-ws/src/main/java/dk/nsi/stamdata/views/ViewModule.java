/**
 * The contents of this file are subject to the Mozilla Public
 * License Version 1.1 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of
 * the License at http://www.mozilla.org/MPL/
 *
 * Software distributed under the License is distributed on an "AS
 * IS" basis, WITHOUT WARRANTY OF ANY KIND, either express or
 * implied. See the License for the specific language governing
 * rights and limitations under the License.
 *
 * Contributor(s): Contributors are attributed in the source code
 * where applicable.
 *
 * The Original Code is "Stamdata".
 *
 * The Initial Developer of the Original Code is Trifork Public A/S.
 *
 * Portions created for the Original Code are Copyright 2011,
 * Lægemiddelstyrelsen. All Rights Reserved.
 *
 * Portions created for the FMKi Project are Copyright 2011,
 * National Board of e-Health (NSI). All Rights Reserved.
 */
package dk.nsi.stamdata.views;

import com.google.common.collect.Maps;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.multibindings.Multibinder;
import com.trifork.stamdata.persistence.Persistent;
import dk.nsi.stamdata.views.autorisationsregisteret.Autorisation;
import dk.nsi.stamdata.views.cpr.BarnRelation;
import dk.nsi.stamdata.views.cpr.ForaeldremyndighedsRelation;
import dk.nsi.stamdata.views.cpr.Person;
import dk.nsi.stamdata.views.cpr.UmyndiggoerelseVaergeRelation;
import dk.nsi.stamdata.views.dkma.*;
import dk.nsi.stamdata.views.doseringsforslag.DosageStructure;
import dk.nsi.stamdata.views.doseringsforslag.DosageUnit;
import dk.nsi.stamdata.views.doseringsforslag.DosageVersion;
import dk.nsi.stamdata.views.doseringsforslag.DrugDosageStructureRelation;
import dk.nsi.stamdata.views.sks.Institution;
import dk.nsi.stamdata.views.sor.*;

import java.util.Map;
import java.util.Set;

public class ViewModule extends AbstractModule
{
    @Override
    protected void configure()
    {
        Multibinder<Object> views = Multibinder.newSetBinder(binder(), Object.class, Persistent.class);
        views.addBinding().to(Dosering.class);
        
        // Autorisationsregisteret
        
        views.addBinding().to(Autorisation.class);
        
        // CPR
        
        views.addBinding().to(BarnRelation.class);
        views.addBinding().to(ForaeldremyndighedsRelation.class);
        views.addBinding().to(Person.class);
        views.addBinding().to(UmyndiggoerelseVaergeRelation.class);
        
        // DKMA
        
        views.addBinding().to(Administrationsvej.class);
        views.addBinding().to(ATC.class);
        views.addBinding().to(Beregningsregler.class);
        views.addBinding().to(Dosering.class);
        views.addBinding().to(Doseringskode.class);
        views.addBinding().to(Drug.class);
        views.addBinding().to(EmballagetypeKoder.class);
        views.addBinding().to(Enhedspriser.class);
        views.addBinding().to(Firma.class);
        views.addBinding().to(Formbetegnelse.class);
        views.addBinding().to(Indholdsstoffer.class);
        views.addBinding().to(Indikation.class);
        views.addBinding().to(Indikationskode.class);
        views.addBinding().to(Klausulering.class);
        views.addBinding().to(LaegemiddelAdministrationsvejRelation.class);
        views.addBinding().to(Laegemiddelnavn.class);
        views.addBinding().to(Medicintilskud.class);
        views.addBinding().to(Opbevaringsbetingelser.class);
        views.addBinding().to(OplysningerOmDosisdispensering.class);
        views.addBinding().to(Pakning.class);
        views.addBinding().to(Pakningskombination.class);
        views.addBinding().to(PakningskombinationerUdenPriser.class);
        views.addBinding().to(Pakningsstoerrelsesenhed.class);
        views.addBinding().to(Priser.class);
        views.addBinding().to(Rekommandation.class);
        views.addBinding().to(SpecialeForNBS.class);
        views.addBinding().to(Styrkeenhed.class);
        views.addBinding().to(Substitution.class);
        views.addBinding().to(SubstitutionAfLaegemidlerUdenFastPris.class);
        views.addBinding().to(TakstVersion.class);
        views.addBinding().to(Tidsenhed.class);
        views.addBinding().to(Tilskudsinterval.class);
        views.addBinding().to(TilskudsprisgrupperPakningsniveau.class);
        views.addBinding().to(UdgaaedeNavne.class);
        views.addBinding().to(Udleveringsbestemmelse.class);
        
        // Doseringsforslag
        
        views.addBinding().to(DosageStructure.class);
        views.addBinding().to(DosageUnit.class);
        views.addBinding().to(DosageVersion.class);
        views.addBinding().to(dk.nsi.stamdata.views.doseringsforslag.Drug.class);
        views.addBinding().to(DrugDosageStructureRelation.class);
        
        // SOR
        
        views.addBinding().to(Apotek.class);
        views.addBinding().to(Praksis.class);
        views.addBinding().to(Sygehus.class);
        views.addBinding().to(SygehusAfdeling.class);
        views.addBinding().to(Yder.class);
        
        // SKS
        
        views.addBinding().to(Institution.class);
    }
    

    @Provides
    @SuppressWarnings("unchecked")
    protected Map<String, Class<? extends View>> provideViewMap(@Persistent Set<Object> entities)
    {
        // Filter out views from the persistent classes.
        
        Map<String, Class<? extends View>> viewMap = Maps.newTreeMap();

        for (Object entity : entities)
        {
            ViewPath annotation = entity.getClass().getAnnotation(ViewPath.class);

            if (entity.getClass().isAnnotationPresent(ViewPath.class))
            {
                Class<? extends View> viewClass = (Class<? extends View>) entity.getClass();

                viewMap.put(annotation.value(), viewClass);
            }
        }

        return viewMap;
    }
}
