/*
 * Copyright (c) 2009-2016 Matthew R. Harrah
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package org.gedcom4j.validate;

import java.util.List;

import org.gedcom4j.Options;
import org.gedcom4j.model.AbstractNameVariation;
import org.gedcom4j.model.PersonalName;
import org.gedcom4j.model.PersonalNameVariation;

/**
 * Validator for {@link PersonalName} objects
 * 
 * @author frizbog1
 */
class PersonalNameValidator extends AbstractValidator {

    /**
     * The personal name being validated
     */
    private final PersonalName pn;

    /**
     * Constructor
     * 
     * @param validator
     *            the {@link Validator} that contains all the findings and options
     * @param pn
     *            the personal name being validated
     */
    PersonalNameValidator(Validator validator, PersonalName pn) {
        this.validator = validator;
        this.pn = pn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void validate() {
        if (pn == null) {
            addError("Personal name was null - cannot validate");
            return;
        }
        mustHaveValue(pn, "basic");
        if (pn.getCitations() == null && Options.isCollectionInitializationEnabled()) {
            if (validator.isAutorepairEnabled()) {
                pn.getCitations(true).clear();
                addInfo("citations collection for personal name was null - autorepaired", pn);
            } else {
                addError("citations collection for personal name is null", pn);
            }
        }
        checkCitations(pn);
        checkCustomTags(pn);
        mustHaveValueOrBeOmitted(pn, "given");
        mustHaveValueOrBeOmitted(pn, "nickname");
        mustHaveValueOrBeOmitted(pn, "prefix");
        mustHaveValueOrBeOmitted(pn, "suffix");
        mustHaveValueOrBeOmitted(pn, "surname");
        mustHaveValueOrBeOmitted(pn, "surnamePrefix");

        new NotesListValidator(validator, pn).validate();
        List<PersonalNameVariation> phonetic = pn.getPhonetic();
        if (phonetic == null && Options.isCollectionInitializationEnabled()) {
            if (validator.isAutorepairEnabled()) {
                pn.getPhonetic(true).clear();
                validator.addInfo("PersonalNameValidator had null list of phonetic name variations - repaired", pn);
            } else {
                validator.addError("PersonalNamevalidator has null list of phonetic name variations", pn);
            }
        } else {
            if (validator.isAutorepairEnabled()) {
                int dups = new DuplicateHandler<>(phonetic).process();
                if (dups > 0) {
                    validator.addInfo(dups + " duplicate phonetic found and removed", pn);
                }
            }

            if (phonetic != null) {
                for (AbstractNameVariation nv : phonetic) {
                    PersonalNameVariation pnv = (PersonalNameVariation) nv;
                    new PersonalNameVariationValidator(validator, pnv).validate();
                }
            }
        }

        List<PersonalNameVariation> romanized = pn.getRomanized();
        if (romanized == null && Options.isCollectionInitializationEnabled()) {
            if (validator.isAutorepairEnabled()) {
                pn.getRomanized(true).clear();
                validator.addInfo("Event had null list of romanized name variations - repaired", pn);
            } else {
                validator.addError("Event has null list of romanized name variations", pn);
            }
        } else {
            if (validator.isAutorepairEnabled()) {
                int dups = new DuplicateHandler<>(romanized).process();
                if (dups > 0) {
                    validator.addInfo(dups + " duplicate romanized variations found and removed", pn);
                }
            }

            if (romanized != null) {
                for (AbstractNameVariation nv : romanized) {
                    PersonalNameVariation pnv = (PersonalNameVariation) nv;
                    new PersonalNameVariationValidator(validator, pnv).validate();
                }
            }
        }
    }

}
