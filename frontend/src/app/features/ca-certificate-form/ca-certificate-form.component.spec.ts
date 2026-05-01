import { ComponentFixture, TestBed } from '@angular/core/testing';

import { CaCertificateFormComponent } from './ca-certificate-form.component';

describe('CaCertificateFormComponent', () => {
  let component: CaCertificateFormComponent;
  let fixture: ComponentFixture<CaCertificateFormComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CaCertificateFormComponent]
    })
    .compileComponents();

    fixture = TestBed.createComponent(CaCertificateFormComponent);
    component = fixture.componentInstance;
    await fixture.whenStable();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
});
