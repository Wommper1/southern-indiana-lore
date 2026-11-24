// Values that change when the project moves, kept in one place.

// Where the code and the verification log live. Links to these only work once the repository is public.
export const REPO_URL = "https://github.com/Wommper1/southern-indiana-lore";
export const VERIFICATION_LOG_URL = REPO_URL + "/blob/main/docs/VERIFICATION_LOG.md";
export const ISSUES_URL = REPO_URL + "/issues/new/choose";
// GitHub issue forms accept their fields pre-filled from the address (by field id).
const issueForm = (template, fields) => {
  const q = new URLSearchParams({ template });
  for (const [key, value] of Object.entries(fields || {})) if (value) q.set(key, value);
  return REPO_URL + "/issues/new?" + q.toString();
};
/** A "Data correction" issue with the site name and page already filled in. */
export const correctionUrl = (site, pageUrl) =>
  issueForm("data_correction.yml", { title: "Correction: " + site.title, site: site.title, wrong: "On the web atlas page " + pageUrl + "\n\n" });
/** A "Suggest a site" issue. */
export const suggestSiteUrl = () => issueForm("new_site.yml", { title: "Site suggestion: " });
export const LICENSE_URL = REPO_URL + "/blob/main/LICENSE";
export const NOTICES_URL = REPO_URL + "/blob/main/THIRD_PARTY_NOTICES.md";
export const CC_BY_SA_URL = "https://creativecommons.org/licenses/by-sa/4.0/";
export const OFL_URL = "https://openfontlicense.org/open-font-license-official-text/";

export const OWNER = "Alex Shade Productions";
export const APP_NAME = "Southern Indiana Lore Atlas";
