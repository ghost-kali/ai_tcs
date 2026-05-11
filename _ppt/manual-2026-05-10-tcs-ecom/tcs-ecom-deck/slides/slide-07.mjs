export default async function addSlide(presentation, ctx) {
  const slide = presentation.slides.add();

  const colors = {
    ink: "#0F172A",
    muted: "#475569",
    navy: "#0B2E4A",
    teal: "#00A3A3",
    bg: "#FFFFFF",
    card: "#F8FAFC",
    border: "#E2E8F0",
    good: "#065F46",
  };

  ctx.addShape(slide, { x: 0, y: 0, w: ctx.W, h: ctx.H, fill: colors.bg });
  ctx.addShape(slide, { x: 0, y: 0, w: ctx.W, h: 10, fill: colors.teal });

  ctx.addText(slide, {
    x: 56,
    y: 34,
    w: ctx.W - 112,
    h: 44,
    text: "Outcomes & Next Steps",
    fontSize: 30,
    bold: true,
    color: colors.navy,
    typeface: ctx.fonts.title,
  });

  const leftX = 56;
  const topY = 124;
  const colW = (ctx.W - 56 * 2 - 18) / 2;
  const boxH = 520;

  ctx.addShape(slide, { x: leftX, y: topY, w: colW, h: boxH, fill: colors.card, line: ctx.line(colors.border, 1) });
  ctx.addText(slide, { x: leftX + 20, y: topY + 18, w: colW - 40, h: 24, text: "Outcomes", fontSize: 16, bold: true, color: colors.ink });
  ctx.addText(slide, {
    x: leftX + 20,
    y: topY + 56,
    w: colW - 40,
    h: boxH - 86,
    text: [
      "• Built an end-to-end microservices platform with a single API entry point",
      "• Implemented JWT authentication and identity propagation pattern",
      "• Enforced role-based authorization for protected actions",
      "• Introduced Redis caching and resilience-ready components",
      "• Improved understanding of distributed debugging and API design",
    ].join("\n"),
    fontSize: 13.5,
    color: colors.muted,
  });

  const rightX = leftX + colW + 18;
  ctx.addShape(slide, { x: rightX, y: topY, w: colW, h: boxH, fill: colors.card, line: ctx.line(colors.border, 1) });
  ctx.addText(slide, { x: rightX + 20, y: topY + 18, w: colW - 40, h: 24, text: "Next Steps (Production)", fontSize: 16, bold: true, color: colors.ink });
  ctx.addText(slide, {
    x: rightX + 20,
    y: topY + 56,
    w: colW - 40,
    h: boxH - 86,
    text: [
      "• Restrict service exposure (internal-only networking)",
      "• Add trust proof between gateway and services (mTLS or shared secret)",
      "• Centralize config/secrets via vault or secure config server",
      "• Add CI/CD pipeline + containerization + Kubernetes deployment",
      "• Expand observability: tracing, dashboards, alerts",
      "• Performance tests and capacity planning per service",
    ].join("\n"),
    fontSize: 13.5,
    color: colors.muted,
  });

  ctx.addShape(slide, { x: 56, y: 668, w: ctx.W - 112, h: 40, fill: "#ECFDF5", line: ctx.line("#A7F3D0", 1) });
  ctx.addText(slide, {
    x: 76,
    y: 678,
    w: ctx.W - 152,
    h: 24,
    text: "Ask: approval to proceed with production hardening (network isolation + gateway-to-service trust).",
    fontSize: 13.5,
    bold: true,
    color: colors.good,
  });

  return slide;
}

