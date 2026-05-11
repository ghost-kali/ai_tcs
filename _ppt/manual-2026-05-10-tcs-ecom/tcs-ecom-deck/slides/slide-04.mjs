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
  };

  ctx.addShape(slide, { x: 0, y: 0, w: ctx.W, h: ctx.H, fill: colors.bg });
  ctx.addShape(slide, { x: 0, y: 0, w: ctx.W, h: 10, fill: colors.teal });

  ctx.addText(slide, {
    x: 56,
    y: 34,
    w: ctx.W - 112,
    h: 44,
    text: "Key Features Delivered",
    fontSize: 30,
    bold: true,
    color: colors.navy,
    typeface: ctx.fonts.title,
  });

  const cards = [
    { title: "Authentication", icon: "shield-check", body: ["Sign-in / Sign-up", "JWT + refresh token flow", "Gateway validates JWT once"] },
    { title: "Product & Catalog", icon: "package", body: ["Products, categories", "Public browsing endpoints", "Role-protected admin actions"] },
    { title: "Cart & Orders", icon: "shopping-cart", body: ["Cart operations", "Order placement", "Address management"] },
    { title: "Payments", icon: "credit-card", body: ["Payment service", "External payment integration", "Separation from order domain"] },
    { title: "Resilience", icon: "activity", body: ["Circuit breaker ready", "Fault isolation per service", "Gateway-level stability"] },
    { title: "Observability", icon: "radar", body: ["Actuator endpoints", "Structured logs (ELK ready)", "Tracing hooks available"] },
  ];

  const gridX = 56;
  const gridY = 120;
  const gap = 18;
  const cols = 3;
  const cardW = (ctx.W - 56 * 2 - gap * (cols - 1)) / cols;
  const cardH = 170;

  for (let i = 0; i < cards.length; i += 1) {
    const row = Math.floor(i / cols);
    const col = i % cols;
    const x = gridX + col * (cardW + gap);
    const y = gridY + row * (cardH + gap);

    ctx.addShape(slide, { x, y, w: cardW, h: cardH, fill: colors.card, line: ctx.line(colors.border, 1) });
    await ctx.addLucideIcon(slide, { x: x + 16, y: y + 16, w: 24, h: 24, icon: cards[i].icon, color: colors.teal, strokeWidth: 2.4 });
    ctx.addText(slide, { x: x + 48, y: y + 14, w: cardW - 64, h: 28, text: cards[i].title, fontSize: 16, bold: true, color: colors.ink });
    ctx.addText(slide, {
      x: x + 16,
      y: y + 52,
      w: cardW - 32,
      h: cardH - 64,
      text: cards[i].body.map((v) => `• ${v}`).join("\n"),
      fontSize: 13,
      color: colors.muted,
    });
  }

  ctx.addText(slide, {
    x: 56,
    y: ctx.H - 36,
    w: ctx.W - 112,
    h: 22,
    text: "Slide 4 / 7",
    fontSize: 11,
    color: "#94A3B8",
    align: "right",
  });

  return slide;
}

