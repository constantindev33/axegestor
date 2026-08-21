import { Component, Input } from '@angular/core';

@Component({
  selector: 'app-page-feedback',
  template: `
    @if (loading) {
      <div class="feedback feedback-loading" role="status" aria-live="polite">
        <span class="spinner" aria-hidden="true"></span>
        <span>{{ loadingText }}</span>
      </div>
    }

    @if (!loading && success) {
      <div class="feedback feedback-success" role="status" aria-live="polite">
        {{ success }}
      </div>
    }

    @if (!loading && error) {
      <div class="feedback feedback-error" role="alert">
        {{ error }}
      </div>
    }
  `,
})
export class PageFeedbackComponent {
  @Input() loading = false;
  @Input() loadingText = 'Carregando...';
  @Input() success = '';
  @Input() error = '';
}
