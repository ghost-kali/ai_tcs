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
    warn: "#B45309",
  };

  ctx.addShape(slide, { x: 0, y: 0, w: ctx.W, h: ctx.H, fill: colors.bg });
  ctx.addShape(slide, { x: 0, y: 0, w: ctx.W, h: 10, fill: colors.teal });

  ctx.addText(slide, {
    x: 56,
    y: 34,
    w: ctx.W - 112,
    h: 44,
    text: "Security Model (How it works)",
    fontSize: 30,
    bold: true,
    color: colors.navy,
    typeface: ctx.fonts.title,
  });

  // Flow row
  const flowY = 126;
  const flowH = 132;
  const flowX = 56;
  const flowW = ctx.W - 112;
  const flowPad = 20;
  const flowGap = 40;
  ctx.addShape(slide, { x: flowX, y: flowY, w: flowW, h: flowH, fill: colors.card, line: ctx.line(colors.border, 1) });

  const innerW = flowW - flowPad * 2;
  const stepW = (innerW - flowGap * 2) / 3;
  const stepX = [
    flowX + flowPad,
    flowX + flowPad + stepW + flowGap,
    flowX + flowPad + 2 * (stepW + flowGap),
  ];
  const stepTitles = ["Client Request", "API Gateway (Frontdoor)", "Microservice"];
  const stepBodies = [
    ["Sends cookie or Bearer token", "Calls only gateway URL"],
    ["Validates JWT signature/expiry", "Caches validation (Redis)", "Injects X‑User‑* headers"],
    ["Does NOT parse JWT", "Trusts gateway headers", "Enforces roles/permissions"],
  ];

  for (let i = 0; i < 3; i += 1) {
    ctx.addShape(slide, { x: stepX[i], y: flowY + 18, w: stepW, h: 96, fill: "#FFFFFF", line: ctx.line(colors.border, 1) });
    ctx.addText(slide, { x: stepX[i] + 14, y: flowY + 26, w: stepW - 28, h: 22, text: stepTitles[i], fontSize: 14, bold: true, color: colors.ink });
    ctx.addText(slide, {
      x: stepX[i] + 14,
      y: flowY + 50,
      w: stepW - 28,
      h: 60,
      text: stepBodies[i].map((v) => `• ${v}`).join("\n"),
      fontSize: 12.5,
      color: colors.muted,
    });
    if (i < 2) {
      await ctx.addLucideIcon(slide, { x: stepX[i] + stepW + 12, y: flowY + 52, w: 18, h: 18, icon: "arrow-right", color: colors.teal });
    }
  }

  // Details
  const leftX = 56;
  const topY = 288;
  const colW = (ctx.W - 56 * 2 - 18) / 2;
  const boxH = 300;

  ctx.addShape(slide, { x: leftX, y: topY, w: colW, h: boxH, fill: colors.card, line: ctx.line(colors.border, 1) });
  ctx.addText(slide, { x: leftX + 20, y: topY + 18, w: colW - 40, h: 24, text: "What is centralized", fontSize: 16, bold: true, color: colors.ink });
  ctx.addText(slide, {
    x: leftX + 20,
    y: topY + 54,
    w: colW - 40,
    h: boxH - 80,
    text: [
      "• JWT validation happens once in the gateway",
      "• Public routes are allowed without token",
      "• Gateway adds identity headers:",
      "  - X-User-Id, X-User-Email, X-User-Roles",
    ].join("\n"),
    fontSize: 13.5,
    color: colors.muted,
  });

  const rightX = leftX + colW + 18;
  ctx.addShape(slide, { x: rightX, y: topY, w: colW, h: boxH, fill: colors.card, line: ctx.line(colors.border, 1) });
  ctx.addText(slide, { x: rightX + 20, y: topY + 18, w: colW - 40, h: 24, text: "Production must-haves", fontSize: 16, bold: true, color: colors.ink });
  ctx.addText(slide, {
    x: rightX + 20,
    y: topY + 54,
    w: colW - 40,
    h: boxH - 80,
    text: [
      "• Microservices must be internal-only (no public exposure)",
      "• Add trust proof (mTLS or shared secret header)",
      "• Keep service-level authorization close to business logic",
      "• CORS is for browsers, not for authentication",
    ].join("\n"),
    fontSize: 13.5,
    color: colors.muted,
  });

  ctx.addShape(slide, { x: 56, y: 610, w: ctx.W - 112, h: 64, fill: "#FFFBEB", line: ctx.line("#FDE68A", 1) });
  ctx.addText(slide, {
    x: 76,
    y: 626,
    w: ctx.W - 152,
    h: 40,
    text: "Key point: Gateway-only auth is safe only when services cannot be called directly.",
    fontSize: 14,
    bold: true,
    color: colors.warn,
  });

  ctx.addText(slide, {
    x: 56,
    y: ctx.H - 36,
    w: ctx.W - 112,
    h: 22,
    text: "Slide 5 / 7",
    fontSize: 11,
    color: "#94A3B8",
    align: "right",
  });

  return slide;
}
