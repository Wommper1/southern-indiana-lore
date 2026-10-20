// Tiny DOM helpers. Text is always inserted with textContent / createTextNode, never as HTML.

const SVG_NS = "http://www.w3.org/2000/svg";

function applyAttrs(el, attrs) {
  for (const [name, value] of Object.entries(attrs || {})) {
    if (value === false || value === null || value === undefined) continue;
    if (name === "class") el.setAttribute("class", value);
    else if (name === "dataset") Object.assign(el.dataset, value);
    else if (name.startsWith("on") && typeof value === "function") el.addEventListener(name.slice(2), value);
    else if (name === "text") el.textContent = value;
    else el.setAttribute(name, value === true ? "" : String(value));
  }
}

function appendChildren(el, children) {
  for (const child of children.flat(Infinity)) {
    if (child === null || child === undefined || child === false) continue;
    el.appendChild(typeof child === "object" ? child : document.createTextNode(String(child)));
  }
}

/** h("a", { href: "#/", class: "x" }, "label", otherNode) */
export function h(tag, attrs, ...children) {
  const el = document.createElement(tag);
  applyAttrs(el, attrs);
  appendChildren(el, children);
  return el;
}

export function svg(tag, attrs, ...children) {
  const el = document.createElementNS(SVG_NS, tag);
  applyAttrs(el, attrs);
  appendChildren(el, children);
  return el;
}

export function clear(el) {
  while (el.firstChild) el.removeChild(el.firstChild);
  return el;
}

/** Paragraphs from text with blank-line breaks. */
export function paragraphEls(paras, cls) {
  return paras.map((text) => h("p", cls ? { class: cls } : null, text));
}
