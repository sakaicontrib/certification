/**
 * Copyright (c) 2003-2026 The Apereo Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *             http://opensource.org/licenses/ecl2
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.sakaiproject.certification.api;

import java.util.Date;
import java.util.Objects;

import lombok.EqualsAndHashCode;

/**
 * Records the immutable award date captured when a certificate is first issued to a user.
 *
 * <p>The award date is intentionally immutable. Eligibility data can change after an award is issued, but the date
 * printed on that award must not.</p>
 */
@EqualsAndHashCode(of = "id")
public class CertificateAward {

    private String id;
    private String userId;
    private CertificateDefinition certificateDefinition;
    private Date awardedAt;

    /**
     * Required by Hibernate. Application code should use the public constructor.
     */
    protected CertificateAward() {
    }

    public CertificateAward(String userId, CertificateDefinition certificateDefinition, Date awardedAt) {
        this.userId = Objects.requireNonNull(userId, "userId");
        this.certificateDefinition = Objects.requireNonNull(certificateDefinition, "certificateDefinition");
        this.awardedAt = copy(Objects.requireNonNull(awardedAt, "awardedAt"));
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public CertificateDefinition getCertificateDefinition() {
        return certificateDefinition;
    }

    public Date getAwardedAt() {
        return copy(awardedAt);
    }

    private static Date copy(Date value) {
        return value == null ? null : new Date(value.getTime());
    }
}
